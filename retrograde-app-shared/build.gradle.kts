plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":retrograde-util"))

    api(deps.libs.archLifecycleCommonJava8)

    implementation(deps.libs.archPaging)
    implementation(deps.libs.autoDispose)
    implementation(deps.libs.daggerAndroid)
    implementation(deps.libs.daggerAndroidSupport)
    implementation(deps.libs.jna)
    implementation(deps.libs.koptional)
    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.kotlinStdlib)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.retrofitRxJava2)
    implementation(deps.libs.roomRuntime)
    implementation(deps.libs.roomRxJava2)
    implementation(deps.libs.rxJava2)
    implementation(deps.libs.rxRelay2)
    implementation(deps.libs.supportAppCompatV7)
    implementation(deps.libs.supportPrefLeanbackV17)

    kapt(deps.libs.roomCompiler)
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
            path = File("$projectDir/CMakeLists.txt")
        }
    }
}
