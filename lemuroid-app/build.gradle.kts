import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    defaultConfig {
        versionCode = 101
        versionName = "1.8.0-beta2"
        applicationId = "com.swordfish.lemuroid"
    }

    // Since some dependencies are closed source we make a completely free as in free speech variant.
    flavorDimensions("opensource")

    productFlavors {
        create("free") {
            dimension = "opensource"
        }

        create("play") {
            dimension = "opensource"
        }
    }

    // Stripping created some issues with some libretro cores such as ppsspp
    packagingOptions {
        doNotStrip("*/*/*_libretro_android.so")
    }

    signingConfigs {
        maybeCreate("debug").apply {
            storeFile = file("$rootDir/debug.keystore")
        }

        maybeCreate("release").apply {
            storeFile = file("$rootDir/release.jks")
            keyAlias = "lemuroid"
            storePassword = "lemuroid"
            keyPassword = "lemuroid"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs["release"]
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            resValue("string", "lemuroid_name", "Lemuroid")
            resValue("color", "main_color", "#00c64e")
            resValue("color", "main_color_light", "#9de3aa")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            resValue("string", "lemuroid_name", "LemuroiDebug")
            resValue("color", "main_color", "#f44336")
            resValue("color", "main_color_light", "#ef9a9a")
        }
    }

    kotlinOptions {
        this as KotlinJvmOptions
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(project(":lemuroid-metadata-libretro-db"))
    implementation(project(":lemuroid-touchinput"))

    // Only the play version bundles cores. The free version downloads them on demand.
    "playImplementation"(project(":lemuroid-cores"))

    "freeImplementation"(project(":lemuroid-app-ext-free"))
    "playImplementation"(project(":lemuroid-app-ext-play"))

    implementation(deps.libs.androidx.navigation.navigationFragment)
    implementation(deps.libs.androidx.navigation.navigationUi)
    implementation(deps.libs.material)
    implementation(deps.libs.picasso)
    implementation(deps.libs.androidx.appcompat.constraintLayout)
    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.preferences.preferencesKtx)
    implementation(deps.libs.rxbindings.core)
    implementation(deps.libs.rxbindings.appcompat)
    implementation(deps.libs.arch.work.runtime)
    implementation(deps.libs.arch.work.runtimeKtx)
    implementation(deps.libs.arch.work.rxjava2)
    implementation(deps.libs.androidx.lifecycle.commonJava8)
    implementation(deps.libs.androidx.lifecycle.extensions)
    implementation(deps.libs.materialProgressBar)
    implementation(deps.libs.epoxy.expoxy)
    implementation(deps.libs.epoxy.paging)

    kapt(deps.libs.epoxy.processor)
    kapt(deps.libs.androidx.lifecycle.processor)

    implementation(deps.libs.androidx.appcompat.leanback)
    implementation(deps.libs.androidx.appcompat.leanbackPreference)

    implementation(deps.libs.androidx.appcompat.recyclerView)
    implementation(deps.libs.androidx.paging.common)
    implementation(deps.libs.androidx.paging.runtime)
    implementation(deps.libs.androidx.paging.rxjava2)
    implementation(deps.libs.androidx.room.common)
    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.androidx.room.rxjava2)
    implementation(deps.libs.autodispose.android.archComponents)
    implementation(deps.libs.autodispose.android.core)
    implementation(deps.libs.autodispose.core)
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
    implementation(deps.libs.rxKotlin2)

    implementation(deps.libs.libretrodroid)

    kapt(deps.libs.dagger.android.processor)
    kapt(deps.libs.dagger.compiler)
}
