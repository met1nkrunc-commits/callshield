# ─── Kotlin ──────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }

# ─── Hilt / Dagger ───────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint *;
}

# ─── Hilt WorkManager ────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.hilt.work.HiltWorkerFactory { *; }

# ─── Room ────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }

# ─── Gson ────────────────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes EnclosingMethod
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.callshield.app.data.remote.dto.** { *; }

# ─── Kotlinx Serialization ───────────────────────────────────────────────────
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.bengel.shared.**$$serializer { *; }
-keepclassmembers class com.bengel.shared.** {
    *** Companion;
}
-keepclasseswithmembers class com.bengel.shared.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.bengel.shared.data.remote.** { *; }
-keep class com.bengel.shared.domain.model.** { *; }
-keep class com.bengel.shared.core.** { *; }
-keep class com.bengel.shared.ml.** { *; }

# ─── OkHttp + Retrofit ───────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ─── Ktor (transitive from shared module) ────────────────────────────────────
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# ─── TFLite Task Library ─────────────────────────────────────────────────────
-keep class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**
-keepclassmembers class org.tensorflow.** { *; }

# ─── Coroutines ──────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ─── Android components (services survive obfuscation) ───────────────────────
-keep class com.callshield.app.service.** { *; }
-keep class com.callshield.app.CallShieldApplication { *; }

# ─── Compose ─────────────────────────────────────────────────────────────────
-dontwarn androidx.compose.**

# ─── Google Play Billing ─────────────────────────────────────────────────────
-keep class com.android.billingclient.** { *; }

# ─── Accompanist ─────────────────────────────────────────────────────────────
-dontwarn com.google.accompanist.**
