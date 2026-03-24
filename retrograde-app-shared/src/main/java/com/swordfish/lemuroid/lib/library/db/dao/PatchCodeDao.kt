package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.swordfish.lemuroid.lib.library.db.entity.PatchCode
import kotlinx.coroutines.flow.Flow

@Dao
interface PatchCodeDao {
    @Query("SELECT * FROM patch_codes WHERE gameId = :gameId ORDER BY id ASC")
    fun getCodesForGame(gameId: Int): Flow<List<PatchCode>>

    @Query("SELECT * FROM patch_codes WHERE gameId = :gameId ORDER BY id ASC")
    suspend fun getAllCodesForGame(gameId: Int): List<PatchCode>

    @Query("SELECT * FROM patch_codes WHERE gameId = :gameId AND enabled = 1 ORDER BY id ASC")
    suspend fun getEnabledCodesForGame(gameId: Int): List<PatchCode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(code: PatchCode): Long

    @Update
    suspend fun update(code: PatchCode)

    @Delete
    suspend fun delete(code: PatchCode)

    @Query("DELETE FROM patch_codes WHERE gameId = :gameId")
    suspend fun deleteAllForGame(gameId: Int)
}
