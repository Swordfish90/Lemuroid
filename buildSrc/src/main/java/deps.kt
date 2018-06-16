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
        const val dagger          = "2.16"
        const val gms             = "12.0.0"
        const val googleApiClient = "1.23.0"
        const val kotlin          = "1.2.50"
        const val koptional       = "1.5.0"
        const val moshi           = "1.6.0"
        const val retrofit        = "2.4.0"
        const val room            = "1.1.0"
        const val support         = "27.1.0"
        const val okHttp          = "3.10.0"
    }

    object libs {
        const val ankoCoroutines               = "org.jetbrains.anko:anko-coroutines:0.10.5"
        const val archLifecycleCommonJava8     = "android.arch.lifecycle:common-java8:${versions.arch}"
        const val archPaging                   = "android.arch.paging:runtime:1.0.0"
        const val autoDispose                  = "com.uber.autodispose:autodispose:${versions.autoDispose}"
        const val autoDisposeAndroid           = "com.uber.autodispose:autodispose-android:${versions.autoDispose}"
        const val autoDisposeAndroidArch       = "com.uber.autodispose:autodispose-android-archcomponents:${versions.autoDispose}"
        const val autoDisposeAndroidArchKotlin = "com.uber.autodispose:autodispose-android-archcomponents-kotlin:${versions.autoDispose}"
        const val autoDisposeAndroidKotlin     = "com.uber.autodispose:autodispose-android-kotlin:${versions.autoDispose}"
        const val autoDisposeKotlin            = "com.uber.autodispose:autodispose-kotlin:${versions.autoDispose}"
        const val bugsnagAndroidNdk            = "com.bugsnag:bugsnag-android-ndk:1.1.3"
        const val dagger                       = "com.google.dagger:dagger:${versions.dagger}"
        const val daggerAndroid                = "com.google.dagger:dagger-android:${versions.dagger}"
        const val daggerAndroidProcessor       = "com.google.dagger:dagger-android-processor:${versions.dagger}"
        const val daggerAndroidSupport         = "com.google.dagger:dagger-android-support:${versions.dagger}"
        const val daggerCompiler               = "com.google.dagger:dagger-compiler:${versions.dagger}"
        const val gmsAuth                      = "com.google.android.gms:play-services-auth:${versions.gms}"
        const val googleApiClient              = "com.google.api-client:google-api-client:${versions.googleApiClient}"
        const val googleApiClientAndroid       = "com.google.api-client:google-api-client-android:${versions.googleApiClient}"
        const val googleApiServicesDrive       = "com.google.apis:google-api-services-drive:v3-rev122-1.23.0"
        const val jna                          = "net.java.dev.jna:jna:4.5.1@aar"
        const val koptional                    = "com.gojuno.koptional:koptional:${versions.koptional}"
        const val koptionalRxJava2             = "com.gojuno.koptional:koptional-rxjava2-extensions:${versions.koptional}"
        const val kotlinxCoroutinesAndroid     = "org.jetbrains.kotlinx:kotlinx-coroutines-android:0.23.3"
        const val ktlint                       = "com.github.shyiko:ktlint:0.23.1"
        const val moshi                        = "com.squareup.moshi:moshi:${versions.moshi}"
        const val moshiKotlin                  = "com.squareup.moshi:moshi-kotlin:${versions.moshi}"
        const val okHttp3                      = "com.squareup.okhttp3:okhttp:${versions.okHttp}"
        const val okHttp3Logging               = "com.squareup.okhttp3:logging-interceptor:${versions.okHttp}"
        const val picasso                      = "com.squareup.picasso:picasso:2.71828"
        const val retrofit                     = "com.squareup.retrofit2:retrofit:${versions.retrofit}"
        const val retrofitConverterMoshi       = "com.squareup.retrofit2:converter-moshi:${versions.retrofit}"
        const val retrofitRxJava2              = "com.squareup.retrofit2:adapter-rxjava2:${versions.retrofit}"
        const val roomCompiler                 = "android.arch.persistence.room:compiler:${versions.room}"
        const val roomRuntime                  = "android.arch.persistence.room:runtime:${versions.room}"
        const val roomRxJava2                  = "android.arch.persistence.room:rxjava2:${versions.room}"
        const val rxAndroid2                   = "io.reactivex.rxjava2:rxandroid:2.0.2"
        const val rxJava2                      = "io.reactivex.rxjava2:rxjava:2.1.14"
        const val rxKotlin2                    = "io.reactivex.rxjava2:rxkotlin:2.2.0"
        const val rxPermissions2               = "com.tbruyelle.rxpermissions2:rxpermissions:0.9.5@aar"
        const val rxPreferences                = "com.f2prateek.rx.preferences2:rx-preferences:2.0.0"
        const val rxRelay2                     = "com.jakewharton.rxrelay2:rxrelay:2.0.0"
        const val supportAppCompatV7           = "com.android.support:appcompat-v7:${versions.support}"
        const val supportLeanbackV17           = "com.android.support:leanback-v17:${versions.support}"
        const val supportPaletteV7             = "com.android.support:palette-v7:${versions.support}"
        const val supportPrefLeanbackV17       = "com.android.support:preference-leanback-v17:${versions.support}"
        const val supportRecyclerViewV7        = "com.android.support:recyclerview-v7:${versions.support}"
        const val timber                       = "com.jakewharton.timber:timber:4.7.0"
    }

    object plugins {
        const val android = "com.android.tools.build:gradle:3.1.0"
        const val bugsnag = "com.bugsnag:bugsnag-android-gradle-plugin:3.2.7"
    }
}
