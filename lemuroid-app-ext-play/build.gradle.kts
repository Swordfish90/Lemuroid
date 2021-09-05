plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))

    implementation(deps.libs.retrofit)
    implementation(deps.libs.retrofitRxJava2)
    implementation(deps.libs.play.core)
    implementation(deps.libs.play.coreKtx)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.koptional)

    implementation(deps.libs.gdrive.apiClient)
    implementation(deps.libs.gdrive.apiClientAndroid)
    implementation(deps.libs.gdrive.apiServicesDrive)
    implementation(deps.libs.play.playServices)
    implementation(deps.libs.autodispose.core)
    implementation(deps.libs.autodispose.android.core)
    implementation(deps.libs.autodispose.android.archComponents)
    implementation(deps.libs.androidx.lifecycle.commonJava8)
    kapt(deps.libs.androidx.lifecycle.processor)

    implementation(deps.libs.androidx.leanback.leanback)
    implementation(deps.libs.androidx.appcompat.constraintLayout)
    implementation(deps.libs.material)

    implementation(deps.libs.dagger.core)
}
