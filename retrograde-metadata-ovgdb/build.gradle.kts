plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))

    implementation(deps.libs.dagger)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.kotlinStdlib)
    implementation(deps.libs.roomRuntime)
    implementation(deps.libs.roomRxJava2)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.rxRelay2)

    kapt(deps.libs.daggerCompiler)
    kapt(deps.libs.roomCompiler)
}

android {
    resourcePrefix("ovgdb_")
}
