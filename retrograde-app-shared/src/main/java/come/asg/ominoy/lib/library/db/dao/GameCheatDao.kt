package come.asg.ominoy.lib.library.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import come.asg.ominoy.lib.library.db.entity.GameCheat
import kotlinx.coroutines.flow.Flow

@Dao
interface GameCheatDao {
    @Query("SELECT * FROM game_cheats WHERE gameId = :gameId ORDER BY id ASC")
    fun observeForGame(gameId: Int): Flow<List<GameCheat>>

    @Query("SELECT * FROM game_cheats WHERE gameId = :gameId ORDER BY id ASC")
    suspend fun selectForGame(gameId: Int): List<GameCheat>

    @Insert
    suspend fun insert(cheat: GameCheat): Long

    @Update
    suspend fun update(cheat: GameCheat)

    @Delete
    suspend fun delete(cheat: GameCheat)
}
