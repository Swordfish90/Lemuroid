package com.swordfish.lemuroid.metadata.libretrodb.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "games",
    indices = [
        Index("romName"),
        Index("name"),
        Index("crc32"),
        Index("serial"),
        Index(value = ["system", "normalizedName"]),
    ],
)
data class LibretroRom(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "normalizedName")
    val normalizedName: String,
    @ColumnInfo(name = "system")
    val system: String?,
    @ColumnInfo(name = "romName")
    val romName: String?,
    @ColumnInfo(name = "artwork")
    val artwork: String?,
    @ColumnInfo(name = "developer")
    val developer: String?,
    @ColumnInfo(name = "crc32")
    val crc32: String?,
    @ColumnInfo(name = "serial")
    val serial: String?,
)
