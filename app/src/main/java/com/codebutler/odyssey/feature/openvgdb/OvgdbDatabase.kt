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

package com.codebutler.odyssey.feature.openvgdb

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.codebutler.odyssey.feature.openvgdb.dao.RegionDao
import com.codebutler.odyssey.feature.openvgdb.dao.ReleaseDao
import com.codebutler.odyssey.feature.openvgdb.dao.RomDao
import com.codebutler.odyssey.feature.openvgdb.dao.SystemDao
import com.codebutler.odyssey.feature.openvgdb.entity.Region
import com.codebutler.odyssey.feature.openvgdb.entity.Release
import com.codebutler.odyssey.feature.openvgdb.entity.Rom
import com.codebutler.odyssey.feature.openvgdb.entity.System

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
