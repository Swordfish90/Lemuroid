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

import android.app.Activity
import android.app.Application
import android.content.Context
import com.codebutler.odyssey.BuildConfig
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import javax.inject.Inject

class OdysseyApplication : Application(), HasActivityInjector {

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

    @Inject lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        component = DaggerOdysseyApplicationComponent.builder()
                .application(this)
                .build()
        component.inject(this)

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingActivityInjector
}
