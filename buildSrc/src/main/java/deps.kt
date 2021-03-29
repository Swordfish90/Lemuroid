/* ktlint-disable no-multi-spaces max-line-length */
object deps {
    object android {
        // Sadly we can't have both target sdk 29 and PPSSPP since the core uses ashmem. We need to wait for an upstream fix.
        const val targetSdkVersion  = 29
        const val compileSdkVersion = 29
        const val minSdkVersion     = 23
        const val buildToolsVersion = "29.0.3"
    }

    object versions {
        const val autoDispose     = "1.4.0"
        const val dagger          = "2.19"
        const val gms             = "17.0.0"
        const val koptional       = "1.6.0"
        const val kotlin          = "1.4.30"
        const val okHttp          = "3.12.0"
        const val retrofit        = "2.5.0"
        const val work            = "2.4.0"
        const val navigation      = "2.1.0"
        const val rxbindings      = "3.0.0"
        const val lifecycle       = "2.1.0"
        const val leanback        = "1.1.0-beta01"
        const val googleApiClient = "1.30.9"
        const val paging          = "3.0.0-alpha11"
        const val room            = "2.3.0-alpha04"
        const val libretrodroid   = "799f0cdae"
        const val radialgamepad   = "0.3.1"
    }

    object libs {
        object androidx {
            object appcompat {
                const val appcompat = "androidx.appcompat:appcompat:1.0.2"
                const val recyclerView = "androidx.recyclerview:recyclerview:1.0.0"
                const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.0.0-beta8"
            }
            object leanback {
                const val leanback = "androidx.leanback:leanback:${versions.leanback}"
                const val leanbackPreference = "androidx.leanback:leanback-preference:${versions.leanback}"
                const val leanbackPaging = "androidx.leanback:leanback-paging:1.1.0-alpha06"
                const val tvProvider = "androidx.tvprovider:tvprovider:1.0.0"
            }
            object ktx {
                const val core = "androidx.core:core-ktx:1.1.0"
                const val collection = "androidx.collection:collection-ktx:1.0.0"
            }
            object lifecycle {
                const val commonJava8 = "androidx.lifecycle:lifecycle-common-java8:${versions.lifecycle}"
                const val extensions = "androidx.lifecycle:lifecycle-extensions:${versions.lifecycle}"
                const val processor = "androidx.lifecycle:lifecycle-compiler:${versions.lifecycle}"
                const val reactiveStreams = "android.arch.lifecycle:reactivestreams:1.1.1"
            }
            object preferences {
                const val preferencesKtx = "androidx.preference:preference-ktx:1.1.0"
            }
            object paging {
                const val common = "androidx.paging:paging-common:${versions.paging}"
                const val runtime = "androidx.paging:paging-runtime:${versions.paging}"
                const val rxjava2 = "androidx.paging:paging-rxjava2-ktx:${versions.paging}"
            }
            object navigation {
                const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:${versions.navigation}"
                const val navigationUi = "androidx.navigation:navigation-ui-ktx:${versions.navigation}"
            }
            object room {
                const val common = "androidx.room:room-common:${versions.room}"
                const val compiler = "androidx.room:room-compiler:${versions.room}"
                const val runtime = "androidx.room:room-runtime:${versions.room}"
                const val rxjava2 = "androidx.room:room-rxjava2:${versions.room}"
            }
            const val documentfile = "androidx.documentfile:documentfile:1.0.1"
        }
        object arch {
            object work {
                const val runtime = "androidx.work:work-runtime:${versions.work}"
                const val runtimeKtx = "androidx.work:work-runtime-ktx:${versions.work}"
                const val rxjava2 ="androidx.work:work-rxjava2:${versions.work}"
            }
        }
        object autodispose {
            const val core = "com.uber.autodispose:autodispose:${versions.autoDispose}"
            object android {
                const val core = "com.uber.autodispose:autodispose-android:${versions.autoDispose}"
                const val archComponents = "com.uber.autodispose:autodispose-android-archcomponents:${versions.autoDispose}"
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
        object rxbindings {
            const val core = "com.jakewharton.rxbinding3:rxbinding-core:${versions.rxbindings}"
            const val appcompat = "com.jakewharton.rxbinding3:rxbinding-appcompat:${versions.rxbindings}"
        }
        object kotlin {
            const val stdlib = "stdlib"
            const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC"
            const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${versions.kotlin}"
        }
        object epoxy {
            const val expoxy = "com.airbnb.android:epoxy:3.8.0"
            const val paging = "com.airbnb.android:epoxy-paging:3.8.0"
            const val processor = "com.airbnb.android:epoxy-processor:3.8.0"
        }
        object play {
            const val core = "com.google.android.play:core:1.8.2"
            const val coreKtx = "com.google.android.play:core-ktx:1.8.1"
            const val playServices = "com.google.android.gms:play-services-auth:17.0.0"
            const val billing = "com.android.billingclient:billing-ktx:3.0.0"
        }
        object gdrive {
            const val apiClient            = "com.google.api-client:google-api-client:${versions.googleApiClient}"
            const val apiClientAndroid     = "com.google.api-client:google-api-client-android:${versions.googleApiClient}"
            const val apiServicesDrive     = "com.google.apis:google-api-services-drive:v3-rev20200413-${versions.googleApiClient}"
        }

        const val koptional                = "com.gojuno.koptional:koptional:${versions.koptional}"
        const val koptionalRxJava2         = "com.gojuno.koptional:koptional-rxjava2-extensions:${versions.koptional}"
        const val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.0.1"
        const val ktlint                   = "com.github.shyiko:ktlint:0.29.0"
        const val okio                     = "com.squareup.okio:okio:2.1.0"
        const val okHttp3                  = "com.squareup.okhttp3:okhttp:${versions.okHttp}"
        const val okHttp3Logging           = "com.squareup.okhttp3:logging-interceptor:${versions.okHttp}"
        const val coil                     = "io.coil-kt:coil:1.0.0"
        const val retrofit                 = "com.squareup.retrofit2:retrofit:${versions.retrofit}"
        const val retrofitRxJava2          = "com.squareup.retrofit2:adapter-rxjava2:${versions.retrofit}"
        const val rxAndroid2               = "io.reactivex.rxjava2:rxandroid:2.1.0"
        const val rxJava2                  = "io.reactivex.rxjava2:rxjava:2.2.4"
        const val rxKotlin2                = "io.reactivex.rxjava2:rxkotlin:2.3.0"
        const val rxPermissions2           = "com.github.tbruyelle:rxpermissions:0.10.2"
        const val rxPreferences            = "com.f2prateek.rx.preferences2:rx-preferences:2.0.1"
        const val rxRelay2                 = "com.jakewharton.rxrelay2:rxrelay:2.1.0"
        const val timber                   = "com.jakewharton.timber:timber:4.7.1"
        const val material                 = "com.google.android.material:material:1.2.1"
        const val multitouchGestures       = "com.dinuscxj:multitouchgesturedetector:1.0.0"
        const val guava                    = "com.google.guava:guava:29.0-android"
        const val harmony                  = "com.frybits.harmony:harmony:1.1.4"
        const val rxBilling                = "com.github.betterme-dev:RxBilling:3.0.0"
        const val radialgamepad            = "com.github.Swordfish90:RadialGamePad:${versions.radialgamepad}"
        const val libretrodroid            = "com.github.Swordfish90:LibretroDroid:${versions.libretrodroid}"

        // This will be replaced by native material components when they will be ready.
        const val materialProgressBar      = "me.zhanghai.android.materialprogressbar:library:1.6.1"
    }

    object plugins {
        const val android = "com.android.tools.build:gradle:4.1.0"
        const val navigationSafeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:${versions.navigation}"
    }
}
