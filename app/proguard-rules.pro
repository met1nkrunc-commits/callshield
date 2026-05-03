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

# ─── Gson ────────────────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# ─── Retrofit ────────────────────────────────────────────────────────────────
-keep class retrofit2.** { *; }
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.Platform$Java8

# ─── OkHttp ──────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ─── Ktor ────────────────────────────────────────────────────────────────────
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# ─── Kotlinx Serialization ───────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.bengel.shared.**$$serializer { *; }
-keepclassmembers class com.bengel.shared.** {
    *** Companion;
}
-keepclasseswithmembers class com.bengel.shared.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ─── DataStore ───────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ─── Callshield models (Room entities, domain models) ────────────────────────
-keep class com.callshield.app.data.local.db.entity.** { *; }
-keep class com.callshield.app.domain.model.** { *; }
-keep class com.bengel.shared.domain.model.** { *; }

# ─── AppWidget RemoteViews ───────────────────────────────────────────────────
-keep class com.callshield.app.widget.** { *; }

# ─── R8 / AGP ────────────────────────────────────────────────────────────────
# NOT: -dontoptimize ve -dontshrink KALDIRILDI — R8'i tamamen devre dışı bırakıyor,
# release APK'sı %40-50 büyüyor. Gerçek sorun için hedefli kural eklendi:
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ─── Suppress warnings ───────────────────────────────────────────────────────
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
