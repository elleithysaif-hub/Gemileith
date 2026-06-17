# Gemileith ProGuard Configuration
# Optimized rules for Android launcher application with Compose UI

# ========== KEEP CLASSES ==========

# Keep all public classes in the launcher package
-keep public class com.gemileith.launcher.** { *; }

# Keep all classes in core package
-keep class com.gemileith.core.** { *; }

# Keep AndroidX/Jetpack classes
-keep class androidx.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Keep Google Gemini API models
-keep class com.google.ai.client.generativeai.** { *; }
-keep class com.google.protobuf.** { *; }

# ========== KOTLIN CONFIGURATION ==========

# Keep Kotlin metadata
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Exceptions

# Keep Kotlin Serialization
-keepclassmembers class kotlinx.serialization.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ========== LIFECYCLE/VIEWMODEL ==========

# Keep ViewModel classes
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Keep view model provider factory
-keep class androidx.lifecycle.** { *; }

# ========== DATABASE (Room) ==========

# Keep Room database
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclasseswithmembernames class * {
    @androidx.room.* <fields>;
}
-keepclasseswithmembernames class * {
    @androidx.room.* <methods>;
}

# ========== NETWORK/RETROFIT ==========

# Keep Retrofit classes
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ========== JSON/MOSHI ==========

# Keep Moshi
-keep class com.squareup.moshi.** { *; }
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}

# ========== LOGGING ==========

# Keep logging interceptor
-keep class com.squareup.okhttp3.logging.** { *; }

# ========== DISABLE OPTIMIZATION ==========

# Don't optimize - launcher needs responsive startup
-dontoptimize

# ========== REMOVE DEBUG INFO (optional) ==========

# Uncomment to remove debug information:
# -renamesourcefileattribute SourceFile
