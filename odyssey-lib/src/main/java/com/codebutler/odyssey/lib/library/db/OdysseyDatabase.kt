/*
 * OdysseyDatabase.kt
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

package com.codebutler.odyssey.lib.library.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.codebutler.odyssey.common.db.Converters
import com.codebutler.odyssey.lib.library.db.dao.GameDao
import com.codebutler.odyssey.lib.library.db.entity.Game

@Database(
        entities = arrayOf(Game::class),
        version = 7,
        exportSchema = true)
@TypeConverters(Converters::class)
abstract class OdysseyDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "odyssey"
    }

    abstract fun gameDao(): GameDao
}

