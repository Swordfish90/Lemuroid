/* ktlint-disable no-multi-spaces max-line-length */
object deps {
    object android {
        const val targetSdkVersion  = 35
        const val compileSdkVersion = 35
        const val minSdkVersion     = 23
        const val buildToolsVersion = "34.0.0"
    }

    object versions {
        const val dagger          = "2.19"
        const val gms             = "17.0.0"
        const val kotlin          = "2.0.21"
        const val okHttp          = "4.9.1"
        const val retrofit        = "2.9.0"
        const val work            = "2.9.0"
        const val navigation      = "2.5.2"
        const val lifecycle       = "2.6.1"
        const val leanback        = "1.1.0-rc01"
        const val googleApiClient = "1.32.1"
        const val paging          = "3.2.1"
        const val room            = "2.6.1"
        const val serialization   = "1.2.2"
        const val fragment        = "1.5.1"
        const val activity        = "1.7.2"
        const val libretrodroid   = "0.13.1"
        const val composeBom      = "2024.02.02"
        const val kotlinExtension = "1.4.6"
        const val padkit          = "1.0.0-beta1"

        // Make sure this is compatible with current bom versions:
        // https://developer.android.com/jetpack/compose/bom/bom-mapping
        const val accompanist     = "0.34.0"
    }

    object libs {
        object androidx {
            object appcompat {
                const val appcompat = "androidx.appcompat:appcompat:1.4.2"
                const val recyclerView = "androidx.recyclerview:recyclerview:1.2.1"
                const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
            }
            object leanback {
                const val leanback = "androidx.leanback:leanback:${versions.leanback}"
                const val leanbackPreference = "androidx.leanback:leanback-preference:${versions.leanback}"
                const val leanbackPaging = "androidx.leanback:leanback-paging:1.1.0-alpha07"
                const val tvProvider = "androidx.tvprovider:tvprovider:1.0.0"
            }
            object ktx {
                const val core = "androidx.core:core-ktx:1.8.0"
                const val coreKtx = "androidx.core:core-ktx:1.8.0"
                const val collection = "androidx.collection:collection-ktx:1.1.0"
            }
            object lifecycle {
                const val commonJava8 = "androidx.lifecycle:lifecycle-common-java8:${versions.lifecycle}"
                const val processor = "androidx.lifecycle:lifecycle-compiler:${versions.lifecycle}"
                const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${versions.lifecycle}"
                const val reactiveStreams = "android.arch.lifecycle:reactivestreams:1.1.1"
                const val viewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1"
            }
            object preferences {
                const val preferencesKtx = "androidx.preference:preference-ktx:1.1.1"
            }
            object paging {
                const val common = "androidx.paging:paging-common:${versions.paging}"
                const val runtime = "androidx.paging:paging-runtime:${versions.paging}"
                const val compose = "androidx.paging:paging-compose:${versions.paging}"
            }
            object navigation {
                const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:${versions.navigation}"
                const val navigationUi = "androidx.navigation:navigation-ui-ktx:${versions.navigation}"
                const val compose = "androidx.navigation:navigation-compose:${versions.navigation}"
            }
            object room {
                const val common = "androidx.room:room-common:${versions.room}"
                const val compiler = "androidx.room:room-compiler:${versions.room}"
                const val runtime = "androidx.room:room-runtime:${versions.room}"
                const val paging = "androidx.room:room-paging:${versions.room}"
                const val ktx = "androidx.room:room-ktx:${versions.room}"
            }
            object fragment {
                const val fragment = "androidx.fragment:fragment:${versions.fragment}"
                const val ktx = "androidx.fragment:fragment-ktx:${versions.fragment}"
            }
            const val documentfile = "androidx.documentfile:documentfile:1.0.1"
            object activity {
                const val activity = "androidx.activity:activity:${versions.activity}"
                const val activityKtx = "androidx.activity:activity-ktx:${versions.activity}"
                const val compose = "androidx.activity:activity-compose:${versions.activity}"
            }
            object compose {
                const val composeBom = "androidx.compose:compose-bom:${versions.composeBom}"
                const val material3 = "androidx.compose.material3:material3"
                const val extendedIcons = "androidx.compose.material:material-icons-extended"
                const val tooling = "androidx.compose.ui:ui-tooling"
                const val toolingPreview = "androidx.compose.ui:ui-tooling-preview"
                const val geometry = "androidx.compose.ui:ui-geometry"
                const val runtime = "androidx.compose.runtime:runtime"
                const val constraintLayout = "androidx.constraintlayout:constraintlayout-compose:1.1.0"
                const val unit = "androidx.compose.ui:ui-unit-android"
                const val ui = "androidx.compose.ui:ui"

                object accompanist {
                    const val systemUiController = "com.google.accompanist:accompanist-systemuicontroller:${versions.accompanist}"
                    const val navigationMaterial = "com.google.accompanist:accompanist-navigation-material:${versions.accompanist}"
                    const val drawablePainter = "com.google.accompanist:accompanist-drawablepainter:${versions.accompanist}"
                }
            }
            const val profileInstaller = "androidx.profileinstaller:profileinstaller:1.3.1"
        }
        object arch {
            object work {
                const val runtime = "androidx.work:work-runtime:${versions.work}"
                const val runtimeKtx = "androidx.work:work-runtime-ktx:${versions.work}"
            }
        }
        object dagger {
            const val core = "com.google.dagger:dagger:${versions.dagger}"
            const val compiler = "com.google.dagger:dagger-compiler:${versions.dagger}"
            object android {
                const val core = "com.google.dagger:dagger-android:${versions.dagger}"
                const val processor = "com.google.dagger:dagger-android-processor:${versions.dagger}"
                const val support = "com.google.dagger:dagger-android-support:${versions.dagger}"
            }
        }
        object kotlin {
            const val stdlib = "stdlib"
            const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-core:${versions.serialization}"
            const val serializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:${versions.serialization}"
        }
        object play {
            const val review = "com.google.android.play:review:2.0.0"
            const val reviewKtx = "com.google.android.play:review-ktx:2.0.0"
            const val featureDelivery = "com.google.android.play:feature-delivery:2.1.0"
            const val featureDeliveryKtx = "com.google.android.play:feature-delivery-ktx:2.1.0"
            const val playServices = "com.google.android.gms:play-services-auth:17.0.0"
            const val coroutine = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4"
        }
        object gdrive {
            const val apiClient            = "com.google.api-client:google-api-client:${versions.googleApiClient}"
            const val apiClientAndroid     = "com.google.api-client:google-api-client-android:${versions.googleApiClient}"
            const val apiServicesDrive     = "com.google.apis:google-api-services-drive:v3-rev20210725-${versions.googleApiClient}"
        }
        object coil {
            const val coil = "io.coil-kt:coil:2.6.0"
            const val coilCompose = "io.coil-kt:coil-compose:2.6.0"
        }

        object composeSettings {
            const val uiTiles = "com.github.alorma.compose-settings:ui-tiles:2.1.0"
            const val uiTilesExtended = "com.github.alorma.compose-settings:ui-tiles-extended:2.1.0"
            const val diskStorage = "com.github.alorma:compose-settings-storage-disk:2.0.0"
            const val memoryStorage = "com.github.alorma:compose-settings-storage-memory:2.0.0"
        }

        const val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4"
        const val okio                     = "com.squareup.okio:okio:2.10.0"
        const val okHttp3                  = "com.squareup.okhttp3:okhttp:${versions.okHttp}"
        const val retrofit                 = "com.squareup.retrofit2:retrofit:${versions.retrofit}"
        const val flowPreferences          = "com.fredporciuncula:flow-preferences:1.8.0"
        const val timber                   = "com.jakewharton.timber:timber:5.0.1"
        const val material                 = "com.google.android.material:material:1.6.1"
        const val guava                    = "com.google.guava:guava:30.1.1-android"
        const val harmony                  = "com.frybits.harmony:harmony:1.1.9"
        const val startup                  = "androidx.startup:startup-runtime:1.1.1"
        const val composeHtmlText          = "de.charlex.compose.material3:material3-html-text:2.0.0-beta01"
        const val collectionsImmutable     = "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8"
        const val padkit                   = "io.github.swordfish90:padkit:${versions.padkit}"
        const val libretrodroid            = "com.github.Swordfish90:LibretroDroid:${versions.libretrodroid}"
//        const val libretrodroid            = "com.swordfish:libretrodroid:unspecified"
    }

    object plugins {
        const val android = "com.android.tools.build:gradle:8.4.0"
        const val navigationSafeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:${versions.navigation}"
        const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}"
    }
}
