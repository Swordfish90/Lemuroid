package com.swordfish.lemuroid.lib.library.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "datafiles",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("gameId"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("id", unique = true),
        Index("fileUri"),
        Index("gameId"),
        Index("lastIndexedAt"),
    ],
)
data class DataFile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gameId: Int,
    val fileName: String,
    val fileUri: String,
    val lastIndexedAt: Long,
    val path: String?,
) : Serializable
