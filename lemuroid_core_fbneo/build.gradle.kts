plugins {
    id("com.android.dynamic-feature")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {}

dependencies {
    implementation(project(":lemuroid-app"))
    implementation(kotlin(deps.libs.kotlin.stdlib))
}
