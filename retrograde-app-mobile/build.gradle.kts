import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

    implementation(project(":retrograde-touchinput"))

    implementation(deps.libs.androidx.navigation.navigationFragment)
    implementation(deps.libs.androidx.navigation.navigationUi)
    implementation(deps.libs.material)
    implementation(deps.libs.picasso)
    implementation(deps.libs.androidx.appcompat.constraintLayout)

    // TODO All next dependencies might not be correct.

    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.appcompat.leanback)
    implementation(deps.libs.androidx.appcompat.leanbackPreference)
    implementation(deps.libs.androidx.appcompat.palette)
    implementation(deps.libs.androidx.appcompat.recyclerView)
    implementation(deps.libs.androidx.paging.common)
    implementation(deps.libs.androidx.paging.runtime)
    implementation(deps.libs.androidx.paging.rxjava2)
    implementation(deps.libs.androidx.room.common)
    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.androidx.room.rxjava2)
    implementation(deps.libs.autodispose.android.archComponents)
    implementation(deps.libs.autodispose.android.archComponentsKtx)

    implementation(deps.libs.autodispose.android.core)
    implementation(deps.libs.autodispose.android.ktx)
    implementation(deps.libs.autodispose.core)
    implementation(deps.libs.autodispose.ktx)
    implementation(deps.libs.bugsnagAndroid)
    implementation(deps.libs.bugsnagAndroidNdk)
    implementation(deps.libs.dagger.android.core)
    implementation(deps.libs.dagger.android.support)
    implementation(deps.libs.dagger.core)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.kotlinxCoroutinesAndroid)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.okio)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.retrofitRxJava2)
    implementation(deps.libs.rxAndroid2)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.rxPermissions2)
    implementation(deps.libs.rxPreferences)
    implementation(deps.libs.rxRelay2)
    implementation(deps.libs.guava)

    kapt(deps.libs.dagger.android.processor)
    kapt(deps.libs.dagger.compiler)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

//fun askPassword() = "security -q find-generic-password -w -g -l retrograde-release".execute().trim()

/*gradle.taskGraph.whenReady {
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
}*/
