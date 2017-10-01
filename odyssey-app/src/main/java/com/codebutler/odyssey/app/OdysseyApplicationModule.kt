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
import com.codebutler.odyssey.lib.library.provider.local.LocalGameLibraryProvider
import com.codebutler.odyssey.lib.ovgdb.OvgdbManager
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
    fun gameLibrary(db: OdysseyDatabase, ovgdbManager: OvgdbManager)
            = GameLibrary(db, ovgdbManager, listOf(LocalGameLibraryProvider()))

    @Provides
    fun coreManager(app: OdysseyApplication) = CoreManager(OdysseyHttp(OkHttpClient()), File(app.cacheDir, "cores"))
}
