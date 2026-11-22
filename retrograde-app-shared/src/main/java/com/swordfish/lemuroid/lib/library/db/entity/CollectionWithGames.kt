package com.swordfish.lemuroid.lib.library.db.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CollectionWithGames(
    @Embedded
    val collection: GameCollection,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = GameCollectionCrossRef::class,
            parentColumn = "collectionId",
            entityColumn = "gameId",
        ),
    )
    val games: List<Game>,
)
