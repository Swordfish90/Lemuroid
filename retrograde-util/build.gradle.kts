plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "com.swordfish.lemuroid.common"
}

dependencies {
    api(deps.libs.timber)

    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.appcompat.recyclerView)
    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.androidx.documentfile)
    implementation(deps.libs.androidx.preferences.preferencesKtx)
    implementation(deps.libs.androidx.lifecycle.runtime)
    implementation(deps.libs.kotlinxCoroutinesAndroid)
    implementation(deps.libs.okHttp3)

    implementation(deps.libs.androidx.paging.common)
    implementation(deps.libs.androidx.paging.runtime)
}
