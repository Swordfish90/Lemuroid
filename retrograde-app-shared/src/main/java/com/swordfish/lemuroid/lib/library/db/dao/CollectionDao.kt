package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.swordfish.lemuroid.lib.library.db.entity.CollectionWithGames
import com.swordfish.lemuroid.lib.library.db.entity.GameCollection
import com.swordfish.lemuroid.lib.library.db.entity.GameCollectionCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY createdAt ASC")
    fun getAllCollections(): Flow<List<GameCollection>>

    @Transaction
    @Query("SELECT * FROM collections ORDER BY createdAt ASC")
    fun getCollectionsWithGames(): Flow<List<CollectionWithGames>>

    @Query("SELECT collectionId FROM collection_games WHERE gameId = :gameId")
    fun getCollectionIdsForGame(gameId: Int): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: GameCollection): Long

    @Update
    suspend fun updateCollection(collection: GameCollection)

    @Delete
    suspend fun deleteCollection(collection: GameCollection)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addGameToCollection(crossRef: GameCollectionCrossRef)

    @Query("DELETE FROM collection_games WHERE collectionId = :collectionId AND gameId = :gameId")
    suspend fun removeGameFromCollection(collectionId: Int, gameId: Int)
}
