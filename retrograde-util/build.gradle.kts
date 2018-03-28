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
    implementation(deps.libs.roomRuntime)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.supportAppCompatV7)
    implementation(deps.libs.supportRecyclerViewV7)
}
