kotlin

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pakripin.kasir"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pakripin.kasir"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
