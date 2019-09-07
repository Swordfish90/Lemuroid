plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(project(":retrograde-metadata-ovgdb"))

    implementation(deps.libs.rxJava2)
    implementation(deps.libs.rxKotlin2)
    implementation(deps.libs.rxKotlin2)
    implementation(deps.libs.rxAndroid2)
    implementation(deps.libs.rxRelay2)
    implementation(deps.libs.support.constraintLayout)
    implementation(deps.libs.support.appCompatV7)
    implementation(deps.libs.support.supportCompat)

    kapt(deps.libs.dagger.compiler)
}
