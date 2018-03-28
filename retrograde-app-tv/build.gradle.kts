import kotlin.concurrent.thread

plugins {
    id("com.android.application")
    id("io.fabric")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    defaultConfig {
        applicationId = "com.codebutler.retrograde"
        versionCode = 3
        versionName = "0.0.3"
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
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs["release"]
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

    implementation(deps.libs.crashlytics) {
        isTransitive = true
    }

    kapt(deps.libs.daggerAndroidProcessor)
    kapt(deps.libs.daggerCompiler)
}

fun askPassword(): String {
    return "security -q find-generic-password -w -g -l retrograde-release".execute().trim()
}

gradle.taskGraph.whenReady {
    if (hasTask(":retrograde-app-tv:packageRelease")) {
        val password = askPassword()
        android.signingConfigs.getByName("release").apply {
            storePassword = password
            keyPassword = password
        }
    }
}

fun String.execute(wd: String? = null, ignoreExitCode: Boolean = false): String =
        split(" ").execute(wd, ignoreExitCode)

fun List<String>.execute(wd: String? = null, ignoreExitCode: Boolean = false): String {
    val process = ProcessBuilder(this)
            .also { pb -> wd?.let { pb.directory(File(it)) } }
            .start()
    var result = ""
    val errReader = thread { process.errorStream.bufferedReader().forEachLine { println(it) } }
    val outReader = thread {
        process.inputStream.bufferedReader().forEachLine { line ->
            println(line)
            result += line
        }
    }
    process.waitFor()
    outReader.join()
    errReader.join()
    if (process.exitValue() != 0 && !ignoreExitCode) {
        error("Non-zero exit status for `${this.joinToString(separator = " ")}`")
    }
    return result
}
