plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.motionactions.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.motionactions.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    kotlinOptions { jvmTarget = "17" }
}
