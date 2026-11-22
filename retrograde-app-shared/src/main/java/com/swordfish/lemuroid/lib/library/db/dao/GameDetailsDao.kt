package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.swordfish.lemuroid.lib.library.db.entity.GameDetails
import com.swordfish.lemuroid.lib.library.db.entity.GameWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDetailsDao {
    @Query("SELECT * FROM game_details WHERE gameId = :gameId")
    fun getGameDetails(gameId: Int): Flow<GameDetails?>

    @Query("SELECT * FROM game_details WHERE gameId = :gameId")
    suspend fun getGameDetailsDirect(gameId: Int): GameDetails?

    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    fun getGameWithDetails(gameId: Int): Flow<GameWithDetails?>

    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameWithDetailsDirect(gameId: Int): GameWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(details: GameDetails)

    @Update
    suspend fun update(details: GameDetails)

    @Query("DELETE FROM game_details WHERE gameId = :gameId")
    suspend fun deleteDetailsForGame(gameId: Int)

    @Query("UPDATE game_details SET crc32 = :crc32 WHERE gameId = :gameId")
    suspend fun updateCrc32(gameId: Int, crc32: String)

    @Query("SELECT gameId, rating FROM game_details WHERE rating IS NOT NULL AND rating > 0")
    fun getAllRatings(): Flow<List<GameRating>>
}

data class GameRating(
    val gameId: Int,
    val rating: Float,
)
