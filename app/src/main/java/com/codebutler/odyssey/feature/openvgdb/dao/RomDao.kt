/*
 * RomDao.kt
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

package com.codebutler.odyssey.feature.openvgdb.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.codebutler.odyssey.feature.openvgdb.entity.Rom
import io.reactivex.Maybe

@Dao
interface RomDao {

    @Query("SELECT * FROM roms WHERE romFileName = :romFileName LIMIT 1")
    fun findByFileName(romFileName: String): Maybe<Rom>

    @Query("SELECT * FROM roms WHERE romHashCRC = :crc LIMIT 1")
    fun findByCRC(crc: String): Maybe<Rom>
}
