plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    defaultConfig {
        versionCode = 175
        versionName = "1.14.1" // Always remember to update Cores Tag!
        applicationId = "com.swordfish.lemuroid"
    }

    if (usePlayDynamicFeatures()) {
        println("Building Google Play version. Bundling dynamic features.")
        dynamicFeatures.addAll(
            setOf(
                ":lemuroid_core_desmume",
                ":lemuroid_core_dosbox_pure",
                ":lemuroid_core_fbneo",
                ":lemuroid_core_fceumm",
                ":lemuroid_core_gambatte",
                ":lemuroid_core_genesis_plus_gx",
                ":lemuroid_core_handy",
                ":lemuroid_core_mame2003_plus",
                ":lemuroid_core_mednafen_ngp",
                ":lemuroid_core_mednafen_pce_fast",
                ":lemuroid_core_mednafen_wswan",
                ":lemuroid_core_melonds",
                ":lemuroid_core_mgba",
                ":lemuroid_core_mupen64plus_next_gles3",
                ":lemuroid_core_pcsx_rearmed",
                ":lemuroid_core_ppsspp",
                ":lemuroid_core_prosystem",
                ":lemuroid_core_snes9x",
                ":lemuroid_core_stella",
                ":lemuroid_core_citra"
            )
        )
    }

    // Since some dependencies are closed source we make a completely free as in free speech variant.
    flavorDimensions("opensource", "cores")

    productFlavors {

        create("free") {
            dimension = "opensource"
        }

        create("play") {
            dimension = "opensource"
        }

        // Include cores in the final apk
        create("bundle") {
            dimension = "cores"
        }

        // Download cores on demand (from GooglePlay or GitHub)
        create("dynamic") {
            dimension = "cores"
        }
    }

    // Stripping created some issues with some libretro cores such as ppsspp
    packagingOptions {
        doNotStrip("*/*/*_libretro_android.so")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/library_release.kotlin_module")
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
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            resValue("string", "lemuroid_name", "LemuroiDebug")
        }
    }

    lintOptions {
        disable += setOf("MissingTranslation", "ExtraTranslation")
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(project(":lemuroid-metadata-libretro-db"))
    implementation(project(":lemuroid-touchinput"))

    "bundleImplementation"(project(":bundled-cores"))

    "freeImplementation"(project(":lemuroid-app-ext-free"))
    "playImplementation"(project(":lemuroid-app-ext-play"))

    implementation(deps.libs.androidx.navigation.navigationFragment)
    implementation(deps.libs.androidx.navigation.navigationUi)
    implementation(deps.libs.material)
    implementation(deps.libs.coil)
    implementation(deps.libs.androidx.appcompat.constraintLayout)
    implementation(deps.libs.androidx.activity.activity)
    implementation(deps.libs.androidx.activity.activityKtx)
    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.preferences.preferencesKtx)
    implementation(deps.libs.arch.work.runtime)
    implementation(deps.libs.arch.work.runtimeKtx)
    implementation(deps.libs.androidx.lifecycle.commonJava8)
    implementation(deps.libs.androidx.lifecycle.reactiveStreams)
    implementation(deps.libs.epoxy.expoxy)
    implementation(deps.libs.epoxy.paging)

    kapt(deps.libs.epoxy.processor)
    kapt(deps.libs.androidx.lifecycle.processor)

    implementation(deps.libs.androidx.leanback.leanback)
    implementation(deps.libs.androidx.leanback.leanbackPreference)
    implementation(deps.libs.androidx.leanback.leanbackPaging)

    implementation(deps.libs.androidx.appcompat.recyclerView)
    implementation(deps.libs.androidx.paging.common)
    implementation(deps.libs.androidx.paging.runtime)
    implementation(deps.libs.androidx.room.common)
    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.androidx.room.rxjava2)
    implementation(deps.libs.androidx.room.ktx)
    implementation(deps.libs.autodispose.android.archComponents)
    implementation(deps.libs.autodispose.android.core)
    implementation(deps.libs.autodispose.core)
    implementation(deps.libs.dagger.android.core)
    implementation(deps.libs.dagger.android.support)
    implementation(deps.libs.dagger.core)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.kotlinxCoroutinesAndroid)
    implementation(deps.libs.kotlinxCoroutinesRxJava2)
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
    implementation(deps.libs.flowPreferences)
    implementation(deps.libs.guava)
    implementation(deps.libs.androidx.documentfile)
    implementation(deps.libs.androidx.leanback.tvProvider)
    implementation(deps.libs.harmony)
    implementation(deps.libs.startup)

    implementation(deps.libs.libretrodroid)

    // Uncomment this when using a local aar file.
    //implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    kapt(deps.libs.dagger.android.processor)
    kapt(deps.libs.dagger.compiler)
}

fun usePlayDynamicFeatures(): Boolean {
    val task = gradle.startParameter.taskRequests.toString()
    return task.contains("Play") && task.contains("Dynamic")
}
