package com.swordfish.lemuroid.lib.library.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "collection_games",
    primaryKeys = ["collectionId", "gameId"],
    foreignKeys = [
        ForeignKey(
            entity = GameCollection::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["collectionId"]),
        Index(value = ["gameId"]),
    ],
)
data class GameCollectionCrossRef(
    val collectionId: Int,
    val gameId: Int,
    val addedAt: Long = System.currentTimeMillis(),
)
