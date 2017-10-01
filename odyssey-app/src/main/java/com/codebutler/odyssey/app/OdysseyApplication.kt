/*
 * OdysseyApplication.kt
 *
 * Copyright (C) 2017 Odyssey Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.app

import android.app.Application
import android.content.Context
import com.codebutler.odyssey.BuildConfig
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class OdysseyApplication : Application() {

    companion object {
        init {
            if (BuildConfig.DEBUG) {
                System.setProperty("jna.debug_load", "true")
                System.setProperty("jna.dump_memory", "true")
            }
        }

        fun get(context: Context) = context.applicationContext as OdysseyApplication
    }

    lateinit var component: OdysseyApplicationComponent

    override fun onCreate() {
        super.onCreate()

        component = DaggerOdysseyApplicationComponent.builder()
                .application(this)
                .module(OdysseyApplicationModule())
                .build()

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }
}
