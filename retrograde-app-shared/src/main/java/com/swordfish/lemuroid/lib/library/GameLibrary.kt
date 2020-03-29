/*
 * GameLibrary.kt
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

package com.swordfish.lemuroid.lib.library

import android.net.Uri
import com.swordfish.lemuroid.common.rx.toSingleAsOptional
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.StorageProviderRegistry
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.gojuno.koptional.rxjava2.filterSome
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.io.File

class GameLibrary(
    private val retrogradedb: RetrogradeDatabase,
    private val providerProviderRegistry: StorageProviderRegistry
) {

    fun indexGames(): Completable {
        val startedAtMs = System.currentTimeMillis()

        return Observable.fromIterable(providerProviderRegistry.enabledProviders).concatMap { provider ->
            provider.listUris()
                .flatMapSingle { uri -> retrieveGameForUri(uri) }
                .buffer(BUFFER_SIZE)
                .doOnNext { pairs -> updateExisting(pairs, startedAtMs) }
                .map { pairs -> filterNotExisting(pairs) }
                .map { pairs -> pairs.map {
                    (uri, _) -> provider.getStorageFile(uri) }.filterNotNull()
                }
                .flatMapSingle { retrieveMetadata(it, provider, startedAtMs) }
                .doOnNext { games: List<Game> ->
                    games.forEach { Timber.d("Insert: $it") }
                    retrogradedb.gameDao().insert(games)
                }
        }
        .doOnComplete { removeDeletedGames(startedAtMs) }
        .doOnComplete {
            Timber.i("Library indexing completed in: ${System.currentTimeMillis() - startedAtMs} ms")
        }
        .ignoreElements()
    }

    private fun retrieveMetadata(
        it: List<StorageFile>,
        provider: StorageProvider,
        startedAtMs: Long
    ): Single<List<Game>> {
        return Observable.fromIterable(it)
                .compose(provider.metadataProvider.transformer(startedAtMs))
                .filterSome()
                .toList()
    }

    private fun updateExisting(pairs: MutableList<Pair<Uri, Optional<Game>>>, startedAtMs: Long) {
        pairs.forEach { (uri, game) -> Timber.d("Game already indexed? $uri ${game is Some}") }
        pairs.filter { (_, game) -> game is Some }
                .map { (_, game) -> game.component1()!!.copy(lastIndexedAt = startedAtMs) }
                .let { games ->
                    games.forEach { Timber.d("Update: $it") }
                    retrogradedb.gameDao().update(games)
                }
    }

    private fun filterNotExisting(pairs: List<Pair<Uri, Optional<Game>>>) =
            pairs.filter { (_, game) -> game is None }

    private fun retrieveGameForUri(uri: Uri): Single<Pair<Uri, Optional<Game>>> {
        Timber.d("Retrieving game for uri: $uri")
        return retrogradedb.gameDao().selectByFileUri(uri.toString())
                .toSingleAsOptional()
                .map { game -> Pair(uri, game) }
    }

    private fun removeDeletedGames(startedAtMs: Long) {
        val games = retrogradedb.gameDao().selectByLastIndexedAtLessThan(startedAtMs)
        retrogradedb.gameDao().delete(games)
    }

    fun getGameRom(game: Game): Single<File> =
            providerProviderRegistry.getProvider(game).getGameRom(game)

    companion object {
        // We batch database updates to avoid unnecessary UI updates.
        const val BUFFER_SIZE = 100
    }
}
