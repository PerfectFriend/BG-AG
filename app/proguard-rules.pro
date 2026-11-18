# -----------------------------------------------------------------------------
# MadGammon ProGuard / R8 Production Hardening Rules
# -----------------------------------------------------------------------------

# Preserve line numbers and essential attributes for debuggable stack traces
-keepattributes SourceFile,LineNumberTable,InnerClasses,EnclosingMethod,Signature,*Annotation*

# --- BouncyCastle Cryptographic Provider ---
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-keep class org.bouncycastle.** { *; }
-keep interface org.bouncycastle.** { *; }
-keepclassmembers class org.bouncycastle.** { *; }

# --- UniFFI Rust Native Bindings ---
-keep class uniffi.** { *; }
-keepclassmembers class uniffi.** { *; }
-dontwarn uniffi.**

# --- Native JNI Methods ---
-keepclasseswithmembernames class * {
    native <methods>;
}

# --- Project Cryptographic and Data Models ---
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.model.** { *; }
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }
-keep class com.example.crypto.** { *; }
-keepclassmembers class com.example.crypto.** { *; }
-keep class com.example.service.** { *; }

# --- AndroidX Room / SQLite Database ---
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class * { *; }

# --- OkHttp, WebSocket and Okio Network Stack ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# --- Kotlin Coroutines & Reflection ---
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# --- Jetpack Compose Runtime ---
-keep class androidx.compose.runtime.** { *; }
