/*
 * RetrogradeApplication.kt
 *
 * Copyright (C) 2017 Retrograde Project
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

package com.codebutler.retrograde.app

import android.content.Context
import com.codebutler.retrograde.BuildConfig
import com.codebutler.retrograde.R
import com.codebutler.retrograde.storage.gdrive.GDriveStorageProvider
import com.crashlytics.android.Crashlytics
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.fabric.sdk.android.Fabric
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

class RetrogradeApplication : DaggerApplication() {
    companion object {
        init {
            if (BuildConfig.DEBUG) {
                System.setProperty("jna.debug_load", "true")
                System.setProperty("jna.dump_memory", "true")
            }
        }

        fun get(context: Context) = context.applicationContext as RetrogradeApplication
    }

    @Inject lateinit var rxTimberTree: RxTimberTree
    @Inject lateinit var rxPrefs: RxSharedPreferences
    @Inject lateinit var gdriveStorageProvider: GDriveStorageProvider

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Fabric.with(this, Crashlytics())
        }

        var isPlanted = false
        rxPrefs.getBoolean(getString(R.string.pref_key_flags_logging)).asObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { value ->
                    gdriveStorageProvider.loggingEnabled = value
                    if (value) {
                        Timber.plant(rxTimberTree)
                        isPlanted = true
                    } else {
                        if (isPlanted) {
                            Timber.uproot(rxTimberTree)
                            isPlanted = false
                        }
                    }
                }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerRetrogradeApplicationComponent.builder()
                .create(this)
    }
}
