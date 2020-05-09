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

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import com.swordfish.lemuroid.lib.library.db.entity.Game
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.intellij.lang.annotations.Language

@Dao
interface GameDao {

    @Language("RoomSql")
    @Query("""
        SELECT
            count(*) totalCount,
            sum(CASE WHEN isFavorite = 1 THEN 1 ELSE 0 END) favoritesCount,
            sum(CASE WHEN lastPlayedAt IS NOT NULL THEN 1 ELSE 0 END) recentsCount
        FROM games
        """)
    fun selectCounts(): Single<GameLibraryCounts>

    @Query("SELECT * FROM games ORDER BY title ASC, id DESC")
    fun selectAll(): DataSource.Factory<Int, Game>

    @Query("SELECT * FROM games WHERE id = :id")
    fun selectById(id: Int): Maybe<Game>

    @Query("SELECT * FROM games WHERE fileUri = :fileUri")
    fun selectByFileUri(fileUri: String): Maybe<Game>

    @Query("SELECT * FROM games WHERE lastIndexedAt < :lastIndexedAt")
    fun selectByLastIndexedAtLessThan(lastIndexedAt: Long): List<Game>

    @Query("SELECT * FROM games WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC")
    fun selectRecentlyPlayed(): DataSource.Factory<Int, Game>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY title ASC")
    fun selectFavorites(): DataSource.Factory<Int, Game>

    @Query("""
        SELECT * FROM games WHERE lastPlayedAt IS NOT NULL AND isFavorite = 0 ORDER BY lastPlayedAt DESC LIMIT :limit
        """)
    fun selectFirstUnfavoriteRecents(limit: Int): LiveData<List<Game>>

    @Query("SELECT * FROM games WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun selectFirstRecents(limit: Int): Observable<List<Game>>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun selectFirstFavorites(limit: Int): LiveData<List<Game>>

    @Query("SELECT * FROM games WHERE lastPlayedAt IS NULL LIMIT :limit")
    fun selectFirstNotPlayed(limit: Int): LiveData<List<Game>>

    @Query("SELECT * FROM games WHERE systemId = :systemId ORDER BY title ASC, id DESC")
    fun selectBySystem(systemId: String): DataSource.Factory<Int, Game>

    @Query("SELECT DISTINCT systemId FROM games ORDER BY systemId ASC")
    fun selectSystems(): LiveData<List<String>>

    @Query("SELECT count(*) count, systemId systemId FROM games GROUP BY systemId")
    fun selectSystemsWithCount(): Observable<List<SystemCount>>

    @Insert
    fun insert(game: Game)

    @Insert
    fun insert(games: List<Game>)

    @Delete
    fun delete(games: List<Game>)

    @Update
    fun update(game: Game)

    @Update
    fun update(games: List<Game>)
}

data class SystemCount(val systemId: String, val count: Int)

data class GameLibraryCounts(val totalCount: Long, val favoritesCount: Long, val recentsCount: Long)

fun GameDao.updateAsync(game: Game): Completable = Completable.fromCallable {
    update(game)
}.subscribeOn(Schedulers.io())
