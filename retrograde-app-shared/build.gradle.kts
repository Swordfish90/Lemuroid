/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlinx-serialization")
}

buildscript {
    repositories {
      //  jcenter()
        mavenCentral()
        google()
        mavenLocal()
        maven { setUrl("https://mirrors.huaweicloud.com/repository/maven") }
    }
}

android {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":lemuroid-touchinput"))

    api(deps.libs.androidx.lifecycle.commonJava8)

    implementation(deps.libs.arch.work.runtime)
    implementation(deps.libs.arch.work.runtimeKtx)
    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.leanback.leanbackPreference)
    implementation(deps.libs.androidx.ktx.collection)
    implementation(deps.libs.androidx.ktx.core)
    implementation(deps.libs.androidx.ktx.coreKtx)
    implementation(deps.libs.androidx.fragment.fragment)
    implementation(deps.libs.androidx.fragment.ktx)
    implementation(deps.libs.androidx.activity.activity)
    implementation(deps.libs.androidx.activity.activityKtx)
    implementation(deps.libs.androidx.ktx.coreKtx)
    implementation(deps.libs.androidx.paging.common)
    implementation(deps.libs.androidx.paging.runtime)
    implementation(deps.libs.androidx.room.common)
    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.androidx.room.ktx)
    implementation(deps.libs.androidx.room.paging)
    implementation(deps.libs.androidx.documentfile)
    implementation(deps.libs.dagger.android.core)
    implementation(deps.libs.dagger.android.support)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.okio)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.kotlin.serialization)
    implementation(deps.libs.kotlin.serializationJson)
    implementation(deps.libs.harmony)
    implementation(deps.libs.material)
    implementation(deps.libs.kotlinxCoroutinesAndroid)
    implementation(deps.libs.flowPreferences)
    implementation(deps.libs.sevenZipJBinding)

    kapt(deps.libs.androidx.room.compiler)
}

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }
    namespace = "com.swordfish.lemuroid.lib"
    kotlinOptions {
        this as KotlinJvmOptions
        jvmTarget = "17"
    }
}
