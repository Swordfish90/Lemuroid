plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "com.swordfish.lemuroid.ext"
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))

    implementation(deps.libs.retrofit)

    // Play Feature Delivery (SplitInstallManager) has been removed.
    // Cores are now downloaded directly from the RetroArch buildbot.
    // Keep Play review/rating and Google Drive save-sync dependencies below.
    implementation(deps.libs.play.review)
    implementation(deps.libs.play.reviewKtx)

    implementation(deps.libs.gdrive.apiClient)
    implementation(deps.libs.gdrive.apiClientAndroid)
    implementation(deps.libs.gdrive.apiServicesDrive)
    implementation(deps.libs.play.playServices)
    implementation(deps.libs.play.coroutine)
    implementation(deps.libs.androidx.lifecycle.commonJava8)
    kapt(deps.libs.androidx.lifecycle.processor)

    implementation(deps.libs.androidx.leanback.leanback)
    implementation(deps.libs.androidx.appcompat.constraintLayout)
    implementation(deps.libs.material)

    implementation(deps.libs.dagger.core)

    implementation(deps.libs.kotlinxCoroutinesAndroid)
}
