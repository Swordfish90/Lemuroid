package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.swordfish.lemuroid.lib.library.db.entity.Cheat
import kotlinx.coroutines.flow.Flow

@Dao
interface CheatDao {
    @Query("SELECT * FROM cheats WHERE gameId = :gameId ORDER BY isMasterCode DESC, orderIndex ASC, id ASC")
    fun getCheatsForGame(gameId: Int): Flow<List<Cheat>>

    @Query("SELECT * FROM cheats WHERE gameId = :gameId ORDER BY isMasterCode DESC, orderIndex ASC, id ASC")
    suspend fun getCheatsForGameSync(gameId: Int): List<Cheat>

    @Query(
        "SELECT * FROM cheats WHERE gameId = :gameId AND enabled = 1 " +
            "ORDER BY isMasterCode DESC, orderIndex ASC, id ASC",
    )
    fun getEnabledCheatsForGame(gameId: Int): Flow<List<Cheat>>

    @Query(
        "SELECT * FROM cheats WHERE gameId = :gameId AND enabled = 1 " +
            "ORDER BY isMasterCode DESC, orderIndex ASC, id ASC",
    )
    suspend fun getEnabledCheatsForGameSync(gameId: Int): List<Cheat>

    @Query("SELECT * FROM cheats WHERE gameId = :gameId AND isMasterCode = 1 ORDER BY orderIndex ASC, id ASC")
    fun getMasterCodesForGame(gameId: Int): Flow<List<Cheat>>

    @Query("SELECT * FROM cheats WHERE gameId = :gameId AND isMasterCode = 1 ORDER BY orderIndex ASC, id ASC")
    suspend fun getMasterCodesForGameSync(gameId: Int): List<Cheat>

    @Query("SELECT * FROM cheats WHERE id = :id")
    suspend fun getCheatById(id: Long): Cheat?

    @Query("SELECT * FROM cheats WHERE masterCodeId = :masterCodeId")
    suspend fun getDependentCheats(masterCodeId: Long): List<Cheat>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheat(cheat: Cheat): Long

    @Update
    suspend fun updateCheat(cheat: Cheat)

    @Delete
    suspend fun deleteCheat(cheat: Cheat)

    @Query("DELETE FROM cheats WHERE id = :id")
    suspend fun deleteCheatById(id: Long)

    @Query("UPDATE cheats SET enabled = :enabled WHERE id = :id")
    suspend fun setCheatEnabled(
        id: Long,
        enabled: Boolean,
    )

    @Query("UPDATE cheats SET enabled = :enabled WHERE masterCodeId = :masterCodeId")
    suspend fun setDependentCheatsEnabled(
        masterCodeId: Long,
        enabled: Boolean,
    )

    @Query("UPDATE cheats SET masterCodeId = NULL WHERE masterCodeId = :masterCodeId")
    suspend fun clearMasterCodeReferences(masterCodeId: Long)
}
