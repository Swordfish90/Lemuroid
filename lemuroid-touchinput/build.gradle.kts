plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))

    implementation(deps.libs.rxJava2)
    implementation(deps.libs.rxKotlin2)
    implementation(deps.libs.rxAndroid2)
    implementation(deps.libs.rxRelay2)
    implementation(deps.libs.androidx.appcompat.constraintLayout)
    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.lifecycle.commonJava8)
    implementation(deps.libs.androidx.lifecycle.extensions)

    implementation(deps.libs.libretrodroid)
    api(deps.libs.radialgamepad)

    implementation(kotlin(deps.libs.kotlin.stdlib))

    kapt(deps.libs.androidx.lifecycle.processor)
}
