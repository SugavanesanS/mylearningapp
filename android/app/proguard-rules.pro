# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/Cellar/android-sdk/24.3.3/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# React Native
-keep class com.facebook.react.** { *; }
-dontwarn com.facebook.react.**

# Hermes JS engine (if enabled)
-keep class com.facebook.hermes.** { *; }
-dontwarn com.facebook.hermes.**

# Keep React Native bridge
-keepclassmembers class * {
    @com.facebook.react.bridge.ReactMethod <methods>;
}

# UCrop (keep its activities + classes)
-keep class com.yalantis.ucrop.** { *; }
-dontwarn com.yalantis.ucrop.**
