plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(project(":retrograde-metadata-ovgdb"))

    implementation(deps.libs.dagger.core)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.moshi)
    implementation(deps.libs.moshiKotlin)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.okHttp3Logging)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.retrofitConverterMoshi)
    implementation(deps.libs.retrofitRxJava2)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.support.prefLeanbackV17)

    kapt(deps.libs.dagger.compiler)
}

android {
    resourcePrefix("archiveorg_")
}
