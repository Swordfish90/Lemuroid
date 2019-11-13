/*
 * OvgdbManager.kt
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

package com.codebutler.retrograde.metadata.ovgdb.db

import android.content.Context
import androidx.room.Room
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService

class OvgdbManager(context: Context, executorService: ExecutorService) {

    companion object {
        private const val ASSET_NAME_DB = "openvgdb.sqlite"

        private const val PREFS_NAME = "openvgdb"
        private const val PREFS_KEY_VERSION = "version"

        private const val DB_NAME = "openvgdb"
        private const val DB_VERSION = 1
    }

    private val dbRelay = BehaviorRelay.create<OvgdbDatabase>()

    val dbReady: Single<OvgdbDatabase> = dbRelay.take(1).singleOrError()

    init {
        executorService.submit {
            copyDbFromAssets(context)
            val db = Room.databaseBuilder(context, OvgdbDatabase::class.java, DB_NAME)
                    .build()
            dbRelay.accept(db)
        }
    }

    /**
     * Database can't be used directly from assets, so copy to filesystem.
     */
    private fun copyDbFromAssets(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dbFile = context.getDatabasePath(DB_NAME)
        // FIXME
        // val dbVersion = prefs.getInt(PREFS_KEY_VERSION, 0)
        // if (!dbFile.exists() || dbVersion != DB_VERSION) {
            context.assets.open(ASSET_NAME_DB).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            val editor = prefs.edit()
            editor.putInt(PREFS_KEY_VERSION, DB_VERSION)
            editor.apply()
        // }
    }
}
