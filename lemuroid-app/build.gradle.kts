plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlinx-serialization")
    id("androidx.baselineprofile")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

buildscript {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
        maven { setUrl("https://mirrors.huaweicloud.com/repository/maven") }
    }

    dependencies {
        classpath(deps.libs.googleServices.googleServices)
        classpath(deps.libs.firebase.firebase)
    }
}

android {
    val versionMajor = 1
    val versionMinor = 10
    val versionPatch = 7

    namespace = "com.swordfish.lemuroid"
    buildFeatures.buildConfig = true

    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    defaultConfig {
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        applicationId = "com.fulldive.extension.fullroid"
        buildConfigField("String", "FLURRY_API_KEY", file("../flurrykey.txt").readText())
        buildConfigField("String", "SERVER_CLIENT_ID", file("../googlekey.txt").readText())
        buildConfigField("String", "ONE_EMULATOR_ATTRIBUTION_SECRET", "\"${System.getenv("ONE_EMULATOR_ATTRIBUTION_SECRET") ?: ""}\"")


        firebaseCrashlytics {
            mappingFileUploadEnabled = false
        }
    }
    flavorDimensions += listOf("opensource")

    productFlavors {

        create("free") {
            dimension = "opensource"
            applicationId = "com.fulldive.extension.fullroid"
            resValue("string", "lemuroid_name", "FullRoid")
        }

        create("pro") {
            dimension = "opensource"
            applicationId = "com.fulldive.extension.fullroid.pro"
            resValue("string", "lemuroid_name", "FullRoid X")
        }
    }

    packagingOptions {
        jniLibs {
            // Stripping created some issues with some libretro cores such as ppsspp
            keepDebugSymbols += setOf("*/*/*_libretro_android.so")
            useLegacyPackaging = true
        }
        resources {
            excludes += setOf("META-INF/DEPENDENCIES", "META-INF/library_release.kotlin_module")
        }
    }

    signingConfigs {
        maybeCreate("release").apply {
            storeFile = file("../keys/keys.jks")
            keyAlias = System.getenv("FULLDIVE_ALIAS")
            storePassword = System.getenv("FULLDIVE_KEYSTORE_PASSWORD")
            keyPassword = System.getenv("FULLDIVE_ALIAS_PASSWORD")
        }
    }

    //Free Bundle
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs["release"]
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            applicationVariants.all {
                val variant = this
                variant.outputs
                    .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                    .forEach { output ->
                        val outputFileName = if (variant.flavorName.contains("pro")) {
                            "FullRoid-v${android.defaultConfig.versionName} X-${variant.buildType.name}.apk"

                        } else {
                            "FullRoid-v${android.defaultConfig.versionName}-${variant.buildType.name}.apk"
                        }
                        output.outputFileName = outputFileName
                    }
            }

            firebaseCrashlytics {
                mappingFileUploadEnabled = true
            }
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            applicationVariants.all {
                val variant = this
                variant.outputs
                    .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                    .forEach { output ->
                        val outputFileName = if (variant.flavorName.contains("pro")) {
                            "FullRoid-v${android.defaultConfig.versionName} X-${variant.buildType.name}.apk"
                        } else {
                            "FullRoid-v${android.defaultConfig.versionName}-${variant.buildType.name}.apk"
                        }
                        output.outputFileName = outputFileName
                    }
            }
            firebaseCrashlytics {
                mappingFileUploadEnabled = false
            }
        }
    }

    lint {
        disable += setOf("MissingTranslation", "ExtraTranslation", "EnsureInitializerMetadata")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = deps.versions.kotlinExtension
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "com.swordfish.lemuroid"
}

androidComponents {
    onVariants(selector().withFlavor("opensource" to "free")) { variant ->
        variant.packaging.jniLibs.excludes.add("**/libppsspp_libretro_android.so")
    }
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(project(":lemuroid-metadata-libretro-db"))
    implementation(project(":lemuroid-touchinput"))

    "baselineProfile"(project(":baselineprofile"))
    implementation(deps.libs.androidx.profileInstaller)

    implementation(project(":bundled-cores"))
    "proImplementation"(project(":bundled-cores-pro"))

    "freeImplementation"(project(":lemuroid-app-ext-free"))
    "proImplementation"(project(":lemuroid-app-ext-free"))

    implementation(deps.libs.androidx.navigation.navigationFragment)
    implementation(deps.libs.androidx.navigation.navigationUi)
    implementation(deps.libs.androidx.navigation.compose)
    implementation(deps.libs.material)
    implementation(deps.libs.coil.coil)
    implementation(deps.libs.coil.coilCompose)
    implementation(deps.libs.androidx.appcompat.constraintLayout)
    implementation(deps.libs.androidx.activity.activity)
    implementation(deps.libs.androidx.activity.activityKtx)
    implementation(deps.libs.androidx.activity.compose)
    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.preferences.preferencesKtx)
    implementation(deps.libs.arch.work.runtime)
    implementation(deps.libs.arch.work.runtimeKtx)
    implementation(deps.libs.androidx.lifecycle.commonJava8)
    implementation(deps.libs.androidx.lifecycle.reactiveStreams)

    kapt(deps.libs.androidx.lifecycle.processor)

    implementation(deps.libs.androidx.leanback.leanback)
    implementation(deps.libs.androidx.leanback.leanbackPreference)
    implementation(deps.libs.androidx.leanback.leanbackPaging)

    implementation(deps.libs.androidx.appcompat.recyclerView)
    implementation(deps.libs.androidx.paging.common)
    implementation(deps.libs.androidx.paging.runtime)
    implementation(deps.libs.androidx.room.common)
    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.androidx.room.ktx)
    implementation(deps.libs.dagger.android.core)
    implementation(deps.libs.dagger.android.support)
    implementation(deps.libs.dagger.core)
    implementation(deps.libs.kotlinxCoroutinesAndroid)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.okio)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.flowPreferences)
    implementation(deps.libs.guava)
    implementation(deps.libs.androidx.documentfile)
    implementation(deps.libs.androidx.leanback.tvProvider)
    implementation(deps.libs.harmony)
    implementation(deps.libs.startup)
    implementation(deps.libs.kotlin.serialization)
    implementation(deps.libs.kotlin.serializationJson)

    implementation(platform(deps.libs.androidx.compose.composeBom))
    implementation(deps.libs.androidx.compose.material3)
    implementation(deps.libs.androidx.compose.constraintLayout)
    debugImplementation(deps.libs.androidx.compose.tooling)
    implementation(deps.libs.androidx.compose.toolingPreview)
    implementation(deps.libs.androidx.compose.extendedIcons)
    implementation(deps.libs.androidx.compose.accompanist.systemUiController)
    implementation(deps.libs.androidx.compose.accompanist.navigationMaterial)
    implementation(deps.libs.androidx.compose.accompanist.drawablePainter)
    implementation(deps.libs.androidx.paging.compose)
    implementation(deps.libs.androidx.lifecycle.viewModelCompose)
    implementation(deps.libs.composeHtmlText)

    implementation(deps.libs.composeSettings.uiTiles)
    implementation(deps.libs.composeSettings.uiTilesExtended)
    implementation(deps.libs.composeSettings.diskStorage)
    implementation(deps.libs.composeSettings.memoryStorage)

    implementation(deps.libs.libretrodroid)
    implementation(deps.libs.lottie)
    // Uncomment this when using a local aar file.
    // implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    kapt(deps.libs.dagger.android.processor)
    kapt(deps.libs.dagger.compiler)

    implementation("com.google.android.gms:play-services-measurement-api:23.0.0")
    implementation("com.google.firebase:firebase-config:23.0.1")


    platform("com.google.firebase:firebase-bom:33.1.2")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation(deps.libs.firebase.crashlytics) {
        isTransitive = true
    }
    implementation(deps.libs.firebase.firebaseAnalytics)
    implementation(deps.libs.firebase.firebaseStorage)


    implementation(deps.libs.flurry.flurry)

    implementation(deps.libs.gdrive.apiClient)
    implementation(deps.libs.gdrive.apiClientAndroid)
    implementation(deps.libs.gdrive.apiServicesDrive)
    implementation(deps.libs.play.playServices)

    implementation(deps.libs.retrofitLogging)
    implementation(deps.libs.gsonAnnotations)
    implementation(deps.libs.retrofitGsonConverter)

    implementation("com.android.installreferrer:installreferrer:2.2")
}

