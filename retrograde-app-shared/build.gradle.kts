plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))

    api(deps.libs.arch.lifecycleCommonJava8)

    implementation(deps.libs.arch.paging)
    implementation(deps.libs.arch.room.runtime)
    implementation(deps.libs.arch.room.rxjava2)
    implementation(deps.libs.arch.work.runtime)
    implementation(deps.libs.arch.work.runtimeKtx)
    implementation(deps.libs.autodispose.core)
    implementation(deps.libs.dagger.android.core)
    implementation(deps.libs.dagger.android.support)
    implementation(deps.libs.jna)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.okio)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.retrofitRxJava2)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.rxKotlin2)
    implementation(deps.libs.rxRelay2)
    implementation(deps.libs.support.appCompatV7)
    implementation(deps.libs.support.prefLeanbackV17)

    kapt(deps.libs.arch.room.compiler)
}

android {
    defaultConfig {
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++11"
            }
        }
        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }
    externalNativeBuild {
        cmake {
            setPath(File("$projectDir/CMakeLists.txt"))
        }
    }
}
