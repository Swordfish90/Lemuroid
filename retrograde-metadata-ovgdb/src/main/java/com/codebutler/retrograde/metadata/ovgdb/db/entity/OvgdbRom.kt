/*
 * Rom.kt
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

package com.codebutler.retrograde.metadata.ovgdb.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        tableName = "roms",
        indices = [Index("romFileName"), Index("romHashCRC")])
data class OvgdbRom(
    @PrimaryKey
    @ColumnInfo(name = "romID")
    val id: Int,

    @ColumnInfo(name = "systemID")
    val systemId: Int,

    @ColumnInfo(name = "regionID")
    val regionId: Int,

    @ColumnInfo(name = "romHashCRC")
    val hashCrc: String?,

    @ColumnInfo(name = "romHashMD5")
    val hashMd5: String?,

    @ColumnInfo(name = "romHashSHA1")
    val hashSha1: String?,

    @ColumnInfo(name = "romSize")
    val size: Int,

    @ColumnInfo(name = "romFileName")
    val fileName: String,

    @ColumnInfo(name = "romExtensionlessFileName")
    val extensionlessFileName: String,

    @ColumnInfo(name = "romSerial")
    val serial: String?,

    @ColumnInfo(name = "romHeader")
    val header: String?,

    @ColumnInfo(name = "romLanguage")
    val language: String?
)
