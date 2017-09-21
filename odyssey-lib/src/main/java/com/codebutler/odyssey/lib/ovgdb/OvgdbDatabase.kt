/*
 * OvgdbDatabase.kt
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

package com.codebutler.odyssey.lib.ovgdb

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.codebutler.odyssey.lib.ovgdb.dao.RegionDao
import com.codebutler.odyssey.lib.ovgdb.dao.ReleaseDao
import com.codebutler.odyssey.lib.ovgdb.dao.RomDao
import com.codebutler.odyssey.lib.ovgdb.dao.SystemDao
import com.codebutler.odyssey.lib.ovgdb.entity.Region
import com.codebutler.odyssey.lib.ovgdb.entity.Release
import com.codebutler.odyssey.lib.ovgdb.entity.Rom
import com.codebutler.odyssey.lib.ovgdb.entity.System

@Database(
        entities = arrayOf(
                Region::class,
                Release::class,
                Rom::class,
                System::class),
        version = 1,
        exportSchema = false)
abstract class OvgdbDatabase : RoomDatabase() {

    abstract fun regionDao(): RegionDao

    abstract fun releaseDao(): ReleaseDao

    abstract fun romDao(): RomDao

    abstract fun systemDao(): SystemDao
}
