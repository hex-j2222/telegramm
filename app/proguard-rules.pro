# ─────────────────────────────────────────────────────────────────
# TelegramVault ProGuard / R8 rules
# ─────────────────────────────────────────────────────────────────

# ── Kotlin ────────────────────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }

# ── AndroidX / Jetpack ───────────────────────────────────────────
-keep class androidx.** { *; }
-dontwarn androidx.**

# ── Hilt ─────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.Module class * { *; }

# ── Room ─────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * { @androidx.room.* <methods>; }

# ── Data Models (Room entities + Parcelable) ─────────────────────
-keep class com.telegramvault.data.model.** { *; }
-keepclassmembers class com.telegramvault.data.model.** { *; }

# ── TDLib ─────────────────────────────────────────────────────────
-keep class org.drinkless.tdlib.** { *; }
-keepclassmembers class org.drinkless.tdlib.** { *; }
-dontwarn org.drinkless.tdlib.**

# ── SQLCipher ────────────────────────────────────────────────────
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# ── Retrofit & OkHttp ────────────────────────────────────────────
-keep class retrofit2.** { *; }
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ── Gson ─────────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ── Glide ────────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** { *; }
-dontwarn com.bumptech.glide.**

# ── Lottie ───────────────────────────────────────────────────────
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# ── Google Play Services Auth ────────────────────────────────────
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ── Coroutines ───────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ── Navigation Safe Args ─────────────────────────────────────────
-keepnames class * extends android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ── Security — obfuscate BuildConfig fields ──────────────────────
# R8 will inline and obfuscate BuildConfig string constants
# Do NOT keep BuildConfig explicitly

# ── Enum values ──────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Native methods ───────────────────────────────────────────────
-keepclasseswithmembernames class * {
    native <methods>;
}

# ── Anti reverse-engineering ─────────────────────────────────────
-repackageclasses 'x'
-allowaccessmodification
-overloadaggressively
-optimizationpasses 5
-dontusemixedcaseclassnames
