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

import android.content.Context
import com.codebutler.odyssey.BuildConfig
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.fabric.sdk.android.Fabric
import timber.log.Timber

class OdysseyApplication : DaggerApplication() {
    companion object {
        init {
            if (BuildConfig.DEBUG) {
                System.setProperty("jna.debug_load", "true")
                System.setProperty("jna.dump_memory", "true")
            }
        }

        fun get(context: Context) = context.applicationContext as OdysseyApplication
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerOdysseyApplicationComponent.builder()
                .create(this)
    }
}
