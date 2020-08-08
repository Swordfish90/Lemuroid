plugins {
    id("com.android.dynamic-feature")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    //implementation(project(":retrograde-util"))
    //implementation(project(":retrograde-app-shared"))

    implementation(kotlin(deps.libs.kotlin.stdlib))
}
