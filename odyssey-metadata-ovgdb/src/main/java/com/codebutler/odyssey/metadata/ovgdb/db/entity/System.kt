/*
 * System.kt
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

package com.codebutler.odyssey.lib.ovgdb.db.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(
        tableName = "systems",
        indices = [Index("systemID")])
data class System(
        @PrimaryKey
        @ColumnInfo(name = "systemID")
        val id: Int,

        @ColumnInfo(name = "systemName")
        val name: String,

        @ColumnInfo(name = "systemShortName")
        val shortName: String,

        @ColumnInfo(name = "systemHeaderSizeBytes")
        val headerSizeBytes: Int?,

        @ColumnInfo(name = "systemHashless")
        val hashless: Int?,

        @ColumnInfo(name = "systemHeader")
        val header: Int?,

        @ColumnInfo(name = "systemSerial")
        val serial: String?,

        @ColumnInfo(name = "systemOEID")
        val oeid: String
)
