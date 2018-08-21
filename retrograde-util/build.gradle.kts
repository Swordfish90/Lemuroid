plugins {
    id("com.android.library")
    id("kotlin-android")
}

dependencies {
    api(deps.libs.timber)

    implementation(deps.libs.jna)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.arch.room.runtime)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.support.appCompatV7)
    implementation(deps.libs.support.recyclerViewV7)
}
