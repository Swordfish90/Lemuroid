plugins {
    id("com.android.application")
    id("com.bugsnag.android.gradle")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    defaultConfig {
        applicationId = "com.codebutler.retrograde"
        versionCode = 5
        versionName = "0.0.5"
    }

    signingConfigs {
        maybeCreate("debug").apply {
            storeFile = file("$rootDir/debug.keystore")
        }

        maybeCreate("release").apply {
            storeFile = file("$rootDir/release.keystore")
            keyAlias = "retrograde"
            storePassword = ""
            keyPassword = ""
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs["release"]
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(project(":retrograde-metadata-ovgdb"))
    implementation(project(":retrograde-storage-gdrive"))
    implementation(project(":retrograde-storage-webdav"))
    implementation(project(":retrograde-storage-archiveorg"))

    implementation(deps.libs.ankoCoroutines)
    implementation(deps.libs.archPaging)
    implementation(deps.libs.autoDispose)
    implementation(deps.libs.autoDisposeAndroid)
    implementation(deps.libs.autoDisposeAndroidArch)
    implementation(deps.libs.autoDisposeAndroidArchKotlin)
    implementation(deps.libs.autoDisposeAndroidKotlin)
    implementation(deps.libs.autoDisposeKotlin)
    implementation(deps.libs.bugsnagAndroidNdk)
    implementation(deps.libs.dagger)
    implementation(deps.libs.daggerAndroid)
    implementation(deps.libs.daggerAndroidSupport)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.kotlinxCoroutinesAndroid)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.picasso)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.retrofitRxJava2)
    implementation(deps.libs.roomRuntime)
    implementation(deps.libs.rxAndroid2)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.rxPermissions2)
    implementation(deps.libs.rxPreferences)
    implementation(deps.libs.rxRelay2)
    implementation(deps.libs.supportAppCompatV7)
    implementation(deps.libs.supportLeanbackV17)
    implementation(deps.libs.supportPaletteV7)
    implementation(deps.libs.supportPrefLeanbackV17)
    implementation(deps.libs.supportRecyclerViewV7)

    kapt(deps.libs.daggerAndroidProcessor)
    kapt(deps.libs.daggerCompiler)
}

fun askPassword() = "security -q find-generic-password -w -g -l retrograde-release".execute().trim()

gradle.taskGraph.whenReady {
    if (hasTask(":retrograde-app-tv:packageRelease")) {
        val password = askPassword()
        android.signingConfigs.getByName("release").apply {
            storePassword = password
            keyPassword = password
        }
    }
}

fun String.execute(): String {
    val process = ProcessBuilder(this.split(" "))
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

    process.waitFor()

    if (process.exitValue() != 0) {
        val errorText = process.errorStream.bufferedReader().use { it.readText() }
        error("Non-zero exit status for `$this`: $errorText")
    }

    return process.inputStream.bufferedReader().use { it.readText() }
}
