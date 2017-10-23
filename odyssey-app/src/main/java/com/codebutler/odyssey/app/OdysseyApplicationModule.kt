/*
 * OdysseyApplicationModule.kt
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

import android.arch.persistence.room.Room
import com.codebutler.odyssey.common.http.OdysseyHttp
import com.codebutler.odyssey.lib.core.CoreManager
import com.codebutler.odyssey.lib.library.GameLibrary
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.provider.GameLibraryProviderRegistry
import com.codebutler.odyssey.lib.library.provider.local.LocalGameLibraryProvider
import com.codebutler.odyssey.lib.ovgdb.OvgdbManager
import com.codebutler.odyssey.provider.webdav.WebDavLibraryProvider
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Module
class OdysseyApplicationModule {

    @Provides
    fun executorService() = Executors.newSingleThreadExecutor()

    @Provides
    fun ovgdbManager(app: OdysseyApplication, executorService: ExecutorService) = OvgdbManager(app, executorService)

    @Provides
    fun odysseyDb(app: OdysseyApplication)
            = Room.databaseBuilder(app, OdysseyDatabase::class.java, OdysseyDatabase.DB_NAME)
                .fallbackToDestructiveMigration()
                .build()

    @Provides
    fun gameLibraryProviderRegistry(app: OdysseyApplication)
            = GameLibraryProviderRegistry(setOf(LocalGameLibraryProvider(app), WebDavLibraryProvider(app)))

    @Provides
    fun gameLibrary(
            db: OdysseyDatabase,
            ovgdbManager: OvgdbManager,
            gameLibraryProviderRegistry: GameLibraryProviderRegistry)
            = GameLibrary(db, ovgdbManager, gameLibraryProviderRegistry)

    @Provides
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .build()

    @Provides
    fun coreManager(app: OdysseyApplication, okHttpClient: OkHttpClient)
            = CoreManager(OdysseyHttp(okHttpClient), File(app.cacheDir, "cores"))
}
