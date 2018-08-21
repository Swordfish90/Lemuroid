/* ktlint-disable no-multi-spaces max-line-length */
object deps {
    object android {
        const val compileSdkVersion = 27
        const val targetSdkVersion  = 27
        const val minSdkVersion     = 23
        const val buildToolsVersion = "27.0.3"
    }

    object versions {
        const val arch            = "1.1.1"
        const val autoDispose     = "0.8.0"
        const val dagger          = "2.17"
        const val gms             = "15.0.0"
        const val googleApiClient = "1.24.1"
        const val kotlin          = "1.2.61"
        const val koptional       = "1.6.0"
        const val moshi           = "1.6.0"
        const val retrofit        = "2.4.0"
        const val room            = "1.1.1"
        const val support         = "27.1.0"
        const val okHttp          = "3.11.0"
        const val work            = "1.0.0-alpha07"
    }

    object libs {
        object arch {
            const val lifecycleCommonJava8 = "android.arch.lifecycle:common-java8:${versions.arch}"
            const val paging = "android.arch.paging:runtime:1.0.1"
            object room {
                const val compiler = "android.arch.persistence.room:compiler:${versions.room}"
                const val runtime = "android.arch.persistence.room:runtime:${versions.room}"
                const val rxjava2 = "android.arch.persistence.room:rxjava2:${versions.room}"
            }
            object work {
                const val runtime = "android.arch.work:work-runtime:${versions.work}"
                const val runtimeKtx = "android.arch.work:work-runtime-ktx:${versions.work}"
            }
        }
        object autodispose {
            const val core = "com.uber.autodispose:autodispose:${versions.autoDispose}"
            const val kotlin = "com.uber.autodispose:autodispose-kotlin:${versions.autoDispose}"
            object android {
                const val core = "com.uber.autodispose:autodispose-android:${versions.autoDispose}"
                const val arch = "com.uber.autodispose:autodispose-android-archcomponents:${versions.autoDispose}"
                const val archKotlin = "com.uber.autodispose:autodispose-android-archcomponents-kotlin:${versions.autoDispose}"
                const val kotlin  = "com.uber.autodispose:autodispose-android-kotlin:${versions.autoDispose}"
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
        object support {
            const val appCompatV7 = "com.android.support:appcompat-v7:${versions.support}"
            const val leanbackV17 = "com.android.support:leanback-v17:${versions.support}"
            const val paletteV7 = "com.android.support:palette-v7:${versions.support}"
            const val prefLeanbackV17 = "com.android.support:preference-leanback-v17:${versions.support}"
            const val recyclerViewV7 = "com.android.support:recyclerview-v7:${versions.support}"
        }
        const val ankoCoroutines           = "org.jetbrains.anko:anko-coroutines:0.10.5"
        const val bugsnagAndroidNdk        = "com.bugsnag:bugsnag-android-ndk:1.1.3"
        const val gmsAuth                  = "com.google.android.gms:play-services-auth:${versions.gms}"
        const val googleApiClient          = "com.google.api-client:google-api-client:${versions.googleApiClient}"
        const val googleApiClientAndroid   = "com.google.api-client:google-api-client-android:${versions.googleApiClient}"
        const val googleApiServicesDrive   = "com.google.apis:google-api-services-drive:v3-rev126-1.24.1"
        const val jna                      = "net.java.dev.jna:jna:4.5.2@aar"
        const val koptional                = "com.gojuno.koptional:koptional:${versions.koptional}"
        const val koptionalRxJava2         = "com.gojuno.koptional:koptional-rxjava2-extensions:${versions.koptional}"
        const val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:0.24.0"
        const val ktlint                   = "com.github.shyiko:ktlint:0.27.0"
        const val moshi                    = "com.squareup.moshi:moshi:${versions.moshi}"
        const val moshiKotlin              = "com.squareup.moshi:moshi-kotlin:${versions.moshi}"
        const val okHttp3                  = "com.squareup.okhttp3:okhttp:${versions.okHttp}"
        const val okHttp3Logging           = "com.squareup.okhttp3:logging-interceptor:${versions.okHttp}"
        const val picasso                  = "com.squareup.picasso:picasso:2.71828"
        const val retrofit                 = "com.squareup.retrofit2:retrofit:${versions.retrofit}"
        const val retrofitConverterMoshi   = "com.squareup.retrofit2:converter-moshi:${versions.retrofit}"
        const val retrofitRxJava2          = "com.squareup.retrofit2:adapter-rxjava2:${versions.retrofit}"
        const val rxAndroid2               = "io.reactivex.rxjava2:rxandroid:2.1.0"
        const val rxJava2                  = "io.reactivex.rxjava2:rxjava:2.2.0"
        const val rxKotlin2                = "io.reactivex.rxjava2:rxkotlin:2.3.0"
        const val rxPermissions2           = "com.tbruyelle.rxpermissions2:rxpermissions:0.9.5@aar"
        const val rxPreferences            = "com.f2prateek.rx.preferences2:rx-preferences:2.0.0"
        const val rxRelay2                 = "com.jakewharton.rxrelay2:rxrelay:2.0.0"
        const val timber                   = "com.jakewharton.timber:timber:4.7.1"
    }

    object plugins {
        const val android = "com.android.tools.build:gradle:3.1.4"
        const val bugsnag = "com.bugsnag:bugsnag-android-gradle-plugin:3.2.8"
    }
}
