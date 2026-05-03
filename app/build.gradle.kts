import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// ── Version from git commit count ────────────────────────────────────────────
val gitCommitCount: Int = try {
    val proc = ProcessBuilder("git", "rev-list", "--count", "HEAD")
        .redirectErrorStream(true) // stderr → stdout, deadlock önler
        .start()
    val output = proc.inputStream.bufferedReader().readText().trim()
    if (proc.waitFor() == 0) output.toIntOrNull() ?: 1 else 1
} catch (_: Exception) { 1 }

// ── Signing from keystore.properties (not committed) ─────────────────────────
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) load(keystorePropertiesFile.inputStream())
}

android {
    namespace  = "com.callshield.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.callshield.app"
        minSdk        = 29
        targetSdk     = 34
        versionCode   = gitCommitCount
        versionName   = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "IPQS_API_KEY",
            "\"${project.findProperty("IPQS_API_KEY") ?: ""}\"")

    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile     = file(keystoreProperties["STORE_FILE"] as String)
                storePassword = keystoreProperties["STORE_PASSWORD"] as String
                keyAlias      = keystoreProperties["KEY_ALIAS"] as String
                keyPassword   = keystoreProperties["KEY_PASSWORD"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            signingConfig     = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            ndk { abiFilters += "arm64-v8a" } // Play Store: sadece gerçek cihaz
        }
        debug {
            isMinifyEnabled     = false
            applicationIdSuffix = ".debug"
            versionNameSuffix   = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions { jvmTarget = "21" }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    androidResources { noCompress += "tflite" }

    sourceSets {
        getByName("main") {
            assets { srcDirs("src/main/assets") }
        }
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.core:core-splashscreen:1.0.1")

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.compose.activity)
    implementation(libs.compose.lifecycle)

    implementation(libs.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore)
    implementation(libs.coroutines.android)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.service)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    implementation(libs.tflite.task.text)
    implementation(libs.tflite.task.audio)

    implementation(libs.work.runtime)
    implementation(libs.accompanist.permissions)

    implementation("com.android.billingclient:billing-ktx:7.0.0")
    implementation("com.google.android.play:review-ktx:2.0.2")

    implementation(project(":shared"))
}
