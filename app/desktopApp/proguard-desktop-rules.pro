# Compose Desktop (release) ProGuard rules
-keep class io.ktor.serialization.kotlinx.** { *; }
-keep class io.ktor.serialization.kotlinx.json.** { *; }
-keep class com.composeship.** { *; }

# Ktor and serialization reflectively access classes
-dontwarn io.ktor.**
-dontwarn kotlinx.serialization.**

# Duplicate classes conflict resolution
-dontnote **
-dontwarn **
