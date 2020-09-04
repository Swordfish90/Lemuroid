plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(deps.libs.play.core)
    implementation(deps.libs.play.coreKtx)
    implementation(deps.libs.rxJava2)
}
