plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "com.swordfish.lemuroid.cores.pro"

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        missingDimensionStrategy("opensource", "pro")
    }
}
