
package com.codebutler.retrograde.metadata.libretrodb.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "games",
    indices = [
        Index(name = "romNameIndex", value = ["romName"]),
        Index(name = "crc32Index", value = ["crc32"])
    ]
)
data class LibretroRom(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "system")
    val system: String?,

    @ColumnInfo(name = "romName")
    val romName: String?,

    @ColumnInfo(name = "developer")
    val developer: String?,

    @ColumnInfo(name = "crc32")
    val crc32: String?
)
