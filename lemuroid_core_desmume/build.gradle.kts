plugins {
    id("com.android.dynamic-feature")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    defaultConfig {
        versionCode = 37
        versionName = "1.6.0-beta1"
    }
}

dependencies {
    implementation(project(":lemuroid-app"))
    //implementation(project(":retrograde-util"))
    //implementation(project(":retrograde-app-shared"))

    implementation(kotlin(deps.libs.kotlin.stdlib))
}
