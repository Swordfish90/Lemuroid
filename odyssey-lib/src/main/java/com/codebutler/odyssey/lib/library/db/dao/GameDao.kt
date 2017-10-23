/*
 * GameDao.kt
 *
 * Copyright (C) 2017 Odyssey Project
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

package com.codebutler.odyssey.lib.library.db.dao

import android.arch.paging.LivePagedListProvider
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import com.codebutler.odyssey.lib.library.db.entity.Game
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@Dao
interface GameDao {

    @Query("SELECT * FROM games ORDER BY title ASC, id DESC")
    fun selectAll(): LivePagedListProvider<Int, Game>

    @Query("SELECT * FROM games WHERE id = :id")
    fun selectById(id: Int): Maybe<Game>

    @Query("SELECT * FROM games WHERE fileUri = :fileUri")
    fun selectByFileUri(fileUri: String): Maybe<Game>

    @Query("SELECT * FROM games WHERE lastIndexedAt < :lastIndexedAt")
    fun selectByLastIndexedAtLessThan(lastIndexedAt: Long): Single<List<Game>>

    @Query("SELECT * FROM games WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC")
    fun selectRecentlyPlayed(): LivePagedListProvider<Int, Game>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY lastPlayedAt DESC")
    fun selectFavorites(): LivePagedListProvider<Int, Game>

    @Query("SELECT * FROM games WHERE systemId = :systemId ORDER BY title ASC, id DESC")
    fun selectBySystem(systemId: String): LivePagedListProvider<Int, Game>

    @Query("SELECT DISTINCT systemId FROM games ORDER BY systemId ASC")
    fun selectSystems(): Single<List<String>>

    @Insert
    fun insert(game: Game)

    @Delete
    fun delete(games: List<Game>)

    @Update
    fun update(game: Game)
}

fun GameDao.updateAsync(game: Game): Completable = Completable.fromCallable {
    update(game)
}.subscribeOn(Schedulers.io())
