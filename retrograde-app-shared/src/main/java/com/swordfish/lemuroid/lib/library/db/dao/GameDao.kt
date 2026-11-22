/*
 * GameDao.kt
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

package com.swordfish.lemuroid.lib.library.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.db.entity.GameWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun selectById(id: Int): Game?

    @Query("SELECT * FROM games WHERE fileUri = :fileUri")
    fun selectByFileUri(fileUri: String): Game?

    @Query("SELECT * FROM games WHERE lastIndexedAt < :lastIndexedAt")
    fun selectByLastIndexedAtLessThan(lastIndexedAt: Long): List<Game>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY COALESCE(customName, title) ASC")
    fun selectFavorites(): PagingSource<Int, Game>

    @Query(
        """
        SELECT * FROM games WHERE lastPlayedAt IS NOT NULL AND isFavorite = 0 ORDER BY lastPlayedAt DESC LIMIT :limit
        """,
    )
    fun selectFirstUnfavoriteRecents(limit: Int): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun selectFirstFavoritesRecents(limit: Int): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT :limit")
    suspend fun asyncSelectFirstRecents(limit: Int): List<Game>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun selectFirstFavorites(limit: Int): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE lastPlayedAt IS NULL LIMIT :limit")
    fun selectFirstNotPlayed(limit: Int): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE lastPlayedAt IS NULL ORDER BY RANDOM() LIMIT :limit")
    suspend fun selectRandomNotPlayed(limit: Int): List<Game>

    @Query(
        """
        SELECT g.* FROM games g
        INNER JOIN game_details d ON g.id = d.gameId
        WHERE d.rating IS NOT NULL AND d.rating > 0
        ORDER BY d.rating DESC, LOWER(COALESCE(g.customName, g.title)) ASC
        LIMIT :limit
        """,
    )
    fun selectTopRated(limit: Int): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE systemId = :systemId ORDER BY COALESCE(customName, title) ASC, id DESC")
    fun selectBySystem(systemId: String): PagingSource<Int, Game>

    @Query("SELECT * FROM games WHERE systemId IN (:systemIds) ORDER BY COALESCE(customName, title) ASC, id DESC")
    fun selectBySystems(systemIds: List<String>): PagingSource<Int, Game>

    @Query(
        """
        SELECT * FROM games
        WHERE systemId IN (:systemIds)
          AND (COALESCE(customName, title) LIKE '%' || :query || '%' OR fileName LIKE '%' || :query || '%')
        ORDER BY COALESCE(customName, title) ASC, id DESC
        """,
    )
    fun searchBySystems(systemIds: List<String>, query: String): PagingSource<Int, Game>

    @Transaction
    @Query("SELECT * FROM games WHERE systemId = :systemId ORDER BY COALESCE(customName, title) ASC, id DESC")
    fun selectBySystemWithDetails(systemId: String): PagingSource<Int, GameWithDetails>

    @Transaction
    @Query("SELECT * FROM games WHERE systemId IN (:systemIds) ORDER BY COALESCE(customName, title) ASC, id DESC")
    fun selectBySystemsWithDetails(systemIds: List<String>): PagingSource<Int, GameWithDetails>

    @Transaction
    @Query(
        """
        SELECT * FROM games
        WHERE systemId IN (:systemIds)
          AND (COALESCE(customName, title) LIKE '%' || :query || '%' OR fileName LIKE '%' || :query || '%')
        ORDER BY COALESCE(customName, title) ASC, id DESC
        """,
    )
    fun searchBySystemsWithDetails(systemIds: List<String>, query: String): PagingSource<Int, GameWithDetails>

    @Query(
        """
        SELECT g.*, d.rating AS rating, d.genres AS genres FROM games g
        LEFT JOIN game_details d ON g.id = d.gameId
        WHERE g.systemId IN (:systemIds)
          AND (
            :query = ''
            OR COALESCE(g.customName, g.title) LIKE '%' || :query || '%'
            OR g.fileName LIKE '%' || :query || '%'
            OR g.developer LIKE '%' || :query || '%'
            OR d.publisher LIKE '%' || :query || '%'
            OR d.genres LIKE '%' || :query || '%'
            OR d.releaseDate LIKE '%' || :query || '%'
            OR d.crc32 LIKE '%' || :query || '%'
            OR d.synopsis LIKE '%' || :query || '%'
          )
        ORDER BY
          CASE WHEN :sortOrder = 'FAVORITES_FIRST' THEN g.isFavorite END DESC,
          CASE WHEN :sortOrder = 'MOST_PLAYED' THEN g.totalPlayTimeMs END DESC,
          CASE WHEN :sortOrder = 'RATING_DESC' THEN (CASE WHEN d.rating IS NULL OR d.rating <= 0 THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'RATING_DESC' THEN d.rating END DESC,
          CASE WHEN :sortOrder = 'RATING_ASC' THEN (CASE WHEN d.rating IS NULL OR d.rating <= 0 THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'RATING_ASC' THEN d.rating END ASC,
          CASE WHEN :sortOrder = 'MOST_RATED' THEN (CASE WHEN d.ratingCount IS NULL OR d.ratingCount <= 0 THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'MOST_RATED' THEN d.ratingCount END DESC,
          CASE WHEN :sortOrder = 'MOST_RATED' THEN d.rating END DESC,
          CASE WHEN :sortOrder = 'RECENTLY_PLAYED' THEN (CASE WHEN g.lastPlayedAt IS NULL THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'RECENTLY_PLAYED' THEN g.lastPlayedAt END DESC,
          CASE WHEN :sortOrder = 'RELEASE_DATE_DESC' THEN (CASE WHEN d.releaseDate IS NULL OR d.releaseDate = '' THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'RELEASE_DATE_DESC' THEN d.releaseDate END DESC,
          CASE WHEN :sortOrder = 'RELEASE_DATE_ASC' THEN (CASE WHEN d.releaseDate IS NULL OR d.releaseDate = '' THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'RELEASE_DATE_ASC' THEN d.releaseDate END ASC,
          CASE WHEN :sortOrder = 'RECENTLY_ADDED' THEN g.lastIndexedAt END DESC,
          CASE WHEN :sortOrder = 'DEVELOPER_ASC' THEN (CASE WHEN g.developer IS NULL OR g.developer = '' THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'DEVELOPER_ASC' THEN LOWER(g.developer) END ASC,
          CASE WHEN :sortOrder = 'NAME_DESC' THEN LOWER(COALESCE(g.customName, g.title)) END DESC,
          LOWER(COALESCE(g.customName, g.title)) ASC,
          g.id DESC
        """,
    )
    fun selectBySystemsSorted(
        systemIds: List<String>,
        query: String,
        sortOrder: String,
    ): Flow<List<GameListItem>>

    @Query(
        """
        SELECT DISTINCT d.genres FROM game_details d
        INNER JOIN games g ON g.id = d.gameId
        WHERE g.systemId IN (:systemIds)
          AND d.genres IS NOT NULL
          AND d.genres != ''
        """,
    )
    fun selectGenresForSystems(systemIds: List<String>): Flow<List<String>>

    @Query("SELECT * FROM games WHERE systemId IN (:systemIds) ORDER BY COALESCE(customName, title) ASC")
    suspend fun selectAllBySystems(systemIds: List<String>): List<Game>

    @Query("SELECT * FROM games ORDER BY COALESCE(customName, title) ASC")
    suspend fun selectAllGames(): List<Game>

    @Query("SELECT DISTINCT systemId FROM games ORDER BY systemId ASC")
    suspend fun selectSystems(): List<String>

    @Query("SELECT count(*) count, systemId systemId FROM games GROUP BY systemId")
    fun selectSystemsWithCount(): Flow<List<SystemCount>>

    @Query(
        """
        SELECT g.*, d.rating AS rating, d.genres AS genres FROM games g
        LEFT JOIN game_details d ON g.id = d.gameId
        WHERE g.isFavorite = 1
          AND (
            :query = ''
            OR COALESCE(g.customName, g.title) LIKE '%' || :query || '%'
            OR g.fileName LIKE '%' || :query || '%'
            OR g.developer LIKE '%' || :query || '%'
            OR d.publisher LIKE '%' || :query || '%'
            OR d.genres LIKE '%' || :query || '%'
            OR d.releaseDate LIKE '%' || :query || '%'
            OR d.crc32 LIKE '%' || :query || '%'
            OR d.synopsis LIKE '%' || :query || '%'
          )
        ORDER BY
          CASE WHEN :sortOrder = 'MOST_PLAYED' THEN g.totalPlayTimeMs END DESC,
          CASE WHEN :sortOrder = 'RATING_DESC' THEN (CASE WHEN d.rating IS NULL OR d.rating <= 0 THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'RATING_DESC' THEN d.rating END DESC,
          CASE WHEN :sortOrder = 'RATING_ASC' THEN (CASE WHEN d.rating IS NULL OR d.rating <= 0 THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'RATING_ASC' THEN d.rating END ASC,
          CASE WHEN :sortOrder = 'MOST_RATED' THEN (CASE WHEN d.ratingCount IS NULL OR d.ratingCount <= 0 THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'MOST_RATED' THEN d.ratingCount END DESC,
          CASE WHEN :sortOrder = 'MOST_RATED' THEN d.rating END DESC,
          CASE WHEN :sortOrder = 'RECENTLY_PLAYED' THEN (CASE WHEN g.lastPlayedAt IS NULL THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'RECENTLY_PLAYED' THEN g.lastPlayedAt END DESC,
          CASE WHEN :sortOrder = 'RELEASE_DATE_DESC' THEN (CASE WHEN d.releaseDate IS NULL OR d.releaseDate = '' THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'RELEASE_DATE_DESC' THEN d.releaseDate END DESC,
          CASE WHEN :sortOrder = 'RELEASE_DATE_ASC' THEN (CASE WHEN d.releaseDate IS NULL OR d.releaseDate = '' THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'RELEASE_DATE_ASC' THEN d.releaseDate END ASC,
          CASE WHEN :sortOrder = 'RECENTLY_ADDED' THEN g.lastIndexedAt END DESC,
          CASE WHEN :sortOrder = 'DEVELOPER_ASC' THEN (CASE WHEN g.developer IS NULL OR g.developer = '' THEN 1 ELSE 0 END) END ASC,
          CASE WHEN :sortOrder = 'DEVELOPER_ASC' THEN LOWER(g.developer) END ASC,
          CASE WHEN :sortOrder = 'NAME_DESC' THEN LOWER(COALESCE(g.customName, g.title)) END DESC,
          LOWER(COALESCE(g.customName, g.title)) ASC,
          g.id DESC
        """,
    )
    fun selectFavoritesSorted(query: String, sortOrder: String): Flow<List<GameListItem>>

    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM games) AS totalGames,
            (SELECT COUNT(DISTINCT systemId) FROM games) AS totalSystems,
            (SELECT COUNT(*) FROM games WHERE lastPlayedAt IS NOT NULL) AS playedGames,
            (SELECT COUNT(*) FROM games WHERE isFavorite = 1) AS favoriteGames,
            (SELECT COUNT(*) FROM game_details WHERE lastScrapedAt IS NOT NULL AND lastScrapedAt > 0 AND (scraperSource IS NULL OR scraperSource != 'None')) AS scrapedGames,
            (SELECT COALESCE(SUM(totalPlayTimeMs), 0) FROM games) AS totalPlayTimeMs
        """,
    )
    fun selectLibraryStats(): Flow<LibraryStatsRaw>

    @Query("UPDATE games SET lastPlayedAt = :lastPlayedAt, totalPlayTimeMs = totalPlayTimeMs + :addedPlayTimeMs WHERE id = :gameId")
    suspend fun recordPlaySession(gameId: Int, lastPlayedAt: Long, addedPlayTimeMs: Long)

    @Insert
    fun insert(games: List<Game>): List<Long>

    @Delete
    fun delete(games: List<Game>)

    @Update
    suspend fun update(game: Game)

    @Update
    fun update(games: List<Game>)
}

data class SystemCount(val systemId: String, val count: Int)

data class LibraryStatsRaw(
    val totalGames: Int,
    val totalSystems: Int,
    val playedGames: Int,
    val favoriteGames: Int,
    val scrapedGames: Int,
    val totalPlayTimeMs: Long,
)

data class GameListItem(
    @Embedded val game: Game,
    val rating: Float?,
    val genres: String?,
)
