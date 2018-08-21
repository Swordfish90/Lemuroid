plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(project(":retrograde-metadata-ovgdb"))

    implementation(deps.libs.autodispose.core)
    implementation(deps.libs.autodispose.android.core)
    implementation(deps.libs.autodispose.android.arch)
    implementation(deps.libs.autodispose.android.archKotlin)
    implementation(deps.libs.autodispose.android.kotlin)
    implementation(deps.libs.autodispose.kotlin)
    implementation(deps.libs.dagger.core)
    implementation(deps.libs.dagger.android.core)
    implementation(deps.libs.dagger.android.support)
    implementation(deps.libs.gmsAuth)
    implementation(deps.libs.googleApiClient)
    implementation(deps.libs.googleApiClientAndroid)
    implementation(deps.libs.googleApiServicesDrive)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.okHttp3Logging)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.retrofitRxJava2)
    implementation(deps.libs.rxAndroid2)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.support.prefLeanbackV17)

    kapt(deps.libs.dagger.compiler)
    kapt(deps.libs.dagger.android.processor)
}

configurations {
    all {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "com.google.http-client", module = "google-http-client-jackson2")
    }
}

android {
    resourcePrefix("gdrive_")
}
