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
    implementation(deps.libs.kotlinxCoroutinesAndroid)
    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.appcompat.constraintLayout)
    implementation(deps.libs.material)
}
