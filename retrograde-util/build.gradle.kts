plugins {
    id("com.android.library")
    id("kotlin-android")
}

dependencies {
    api(deps.libs.timber)

    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.appcompat.recyclerView)
    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.androidx.documentfile)
    implementation(deps.libs.androidx.preferences.preferencesKtx)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.xz)
    implementation(deps.libs.compress)
}
