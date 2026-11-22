package com.swordfish.lemuroid.lib.library.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.io.Serializable

@Entity(
    tableName = "game_details",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("gameId", unique = true),
    ],
)
data class GameDetails(
    @PrimaryKey
    val gameId: Int,
    val synopsis: String? = null,
    val genres: String? = null,
    val publisher: String? = null,
    val releaseDate: String? = null,
    val players: String? = null,
    val rating: Float? = null,
    val ratingCount: Int? = null,
    val bannerUrl: String? = null,
    val logoUrl: String? = null,
    val boxArtFrontUrl: String? = null,
    val boxArtBackUrl: String? = null,
    val screenshotUrlsJson: String? = null,
    val videoUrl: String? = null,
    val crc32: String? = null,
    val scraperSource: String? = null,
    val lastScrapedAt: Long? = null,
) : Serializable

data class GameWithDetails(
    @Embedded
    val game: Game,
    @Relation(
        parentColumn = "id",
        entityColumn = "gameId",
    )
    val details: GameDetails?,
) : Serializable
