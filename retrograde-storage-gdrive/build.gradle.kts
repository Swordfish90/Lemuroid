plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(project(":retrograde-metadata-ovgdb"))

    implementation(deps.libs.autoDispose)
    implementation(deps.libs.autoDisposeAndroid)
    implementation(deps.libs.autoDisposeAndroidArch)
    implementation(deps.libs.autoDisposeAndroidArchKotlin)
    implementation(deps.libs.autoDisposeAndroidKotlin)
    implementation(deps.libs.autoDisposeKotlin)
    implementation(deps.libs.dagger)
    implementation(deps.libs.daggerAndroid)
    implementation(deps.libs.daggerAndroidSupport)
    implementation(deps.libs.gmsAuth)
    implementation(deps.libs.googleApiClient)
    implementation(deps.libs.googleApiClientAndroid)
    implementation(deps.libs.googleApiServicesDrive)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.kotlinStdlib)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.okHttp3Logging)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.retrofitRxJava2)
    implementation(deps.libs.rxAndroid2)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.supportPrefLeanbackV17)

    kapt(deps.libs.daggerCompiler)
    kapt(deps.libs.daggerAndroidProcessor)
}

android {
    resourcePrefix("gdrive_")
}
