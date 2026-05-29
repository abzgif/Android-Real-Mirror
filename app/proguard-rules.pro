# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep CameraX classes
-keep class androidx.camera.** { *; }
-keep interface androidx.camera.** { *; }

# Keep Jetpack Compose
-dontwarn androidx.compose.**

# Keep app classes
-keep class com.realmirror.app.** { *; }
