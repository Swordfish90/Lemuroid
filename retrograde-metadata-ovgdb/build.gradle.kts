plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))

    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.androidx.room.rxjava2)
    implementation(deps.libs.dagger.core)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.rxRelay2)

    kapt(deps.libs.androidx.room.compiler)
    kapt(deps.libs.dagger.compiler)
}

android {
    resourcePrefix("ovgdb_")
}
