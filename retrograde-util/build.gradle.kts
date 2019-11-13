plugins {
    id("com.android.library")
    id("kotlin-android")
}

dependencies {
    api(deps.libs.timber)

    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.appcompat.recyclerView)
    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.jna)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.rxJava2)
}
