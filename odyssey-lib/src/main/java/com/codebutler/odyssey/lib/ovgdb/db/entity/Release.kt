/*
 * Release.kt
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
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "releases")
data class Release(
        @PrimaryKey
        @ColumnInfo(name = "releaseID")
        val id: Int,

        @ColumnInfo(name = "romID")
        val romId: Int,

        @ColumnInfo(name = "releaseTitleName")
        val titleName: String,

        @ColumnInfo(name = "regionLocalizedID")
        val regionLocalizedId: Int,

        @ColumnInfo(name = "releaseCoverFront")
        val coverFront: String?,

        @ColumnInfo(name = "releaseCoverBack")
        val coverBack: String?,

        @ColumnInfo(name = "releaseCoverCart")
        val coverCart: String?,

        @ColumnInfo(name = "releaseCoverDisc")
        val coverDisc: String?,

        @ColumnInfo(name = "releaseDescription")
        val description: String?,

        @ColumnInfo(name = "releaseDeveloper")
        val developer: String?,

        @ColumnInfo(name = "releasePublisher")
        val publisher: String?,

        @ColumnInfo(name = "releaseGenre")
        val genre: String?,

        @ColumnInfo(name = "releaseDate")
        val date: String?,

        @ColumnInfo(name = "releaseReferenceURL")
        val referenceURL: String?,

        @ColumnInfo(name = "releaseReferenceImageURL")
        val referenceImageURL: String?
)
