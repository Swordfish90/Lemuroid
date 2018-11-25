plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))

    api(deps.libs.androidx.lifecycle.commonJava8)
    api(deps.libs.arch.work.runtime)
    api(deps.libs.arch.work.runtimeKtx)

    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.appcompat.leanbackPreference)
    implementation(deps.libs.androidx.ktx.collection)
    implementation(deps.libs.androidx.ktx.core)
    implementation(deps.libs.androidx.paging.common)
    implementation(deps.libs.androidx.paging.runtime)
    implementation(deps.libs.androidx.paging.rxjava2)
    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.androidx.room.rxjava2)
    implementation(deps.libs.autodispose.core)
    implementation(deps.libs.dagger.android.core)
    implementation(deps.libs.dagger.android.support)
    implementation(deps.libs.jna)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.okio)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.retrofitRxJava2)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.rxKotlin2)
    implementation(deps.libs.rxRelay2)

    kapt(deps.libs.androidx.room.compiler)
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
