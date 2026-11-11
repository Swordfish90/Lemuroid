/*
 * RetrogradeDatabase.kt
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

package com.swordfish.lemuroid.lib.library.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.swordfish.lemuroid.lib.library.db.dao.DataFileDao
import com.swordfish.lemuroid.lib.library.db.dao.GameDao
import com.swordfish.lemuroid.lib.library.db.dao.GameSearchDao
import com.swordfish.lemuroid.lib.library.db.entity.DataFile
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Database(
    entities = [Game::class, DataFile::class],
    version = 9,
    exportSchema = true,
)
abstract class RetrogradeDatabase : RoomDatabase() {
    companion object {
        const val DB_NAME = "retrograde"
    }

    abstract fun gameDao(): GameDao

    abstract fun dataFileDao(): DataFileDao

    fun gameSearchDao() = GameSearchDao(gameSearchDaoInternal())

    protected abstract fun gameSearchDaoInternal(): GameSearchDao.Internal
}
