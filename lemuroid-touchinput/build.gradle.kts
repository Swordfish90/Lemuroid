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

    implementation(deps.libs.rxJava2)
    implementation(deps.libs.rxAndroid2)
    implementation(deps.libs.androidx.appcompat.constraintLayout)
    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.lifecycle.commonJava8)
    implementation(deps.libs.material)
    implementation(deps.libs.androidx.preferences.preferencesKtx)

    api(deps.libs.radialgamepad)

    implementation(kotlin(deps.libs.kotlin.stdlib))

    kapt(deps.libs.androidx.lifecycle.processor)
}
