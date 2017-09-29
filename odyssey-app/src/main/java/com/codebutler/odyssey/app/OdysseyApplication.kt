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
import android.arch.persistence.room.Room
import android.content.Context
import com.codebutler.odyssey.BuildConfig
import com.codebutler.odyssey.common.http.OdysseyHttp
import com.codebutler.odyssey.lib.core.CoreManager
import com.codebutler.odyssey.lib.library.GameLibrary
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.ovgdb.OvgdbManager
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.Executors

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

    // FIXME: Dagger goes here
    val executorService by lazy { Executors.newSingleThreadExecutor() }
    val ovgdb by lazy { OvgdbManager(this, executorService) }
    val db by lazy {
        Room.databaseBuilder(this, OdysseyDatabase::class.java, OdysseyDatabase.DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
    val library by lazy { GameLibrary(this, db, ovgdb) }
    val coreManager by lazy { CoreManager(OdysseyHttp(OkHttpClient()), File(cacheDir, "cores")) }
}
