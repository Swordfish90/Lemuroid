/*
 * GameLibrary.kt
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

package com.codebutler.odyssey.lib.library

import android.net.Uri
import android.util.Log
import com.codebutler.odyssey.common.rx.toSingleAsOptional
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.library.provider.GameLibraryProvider
import com.codebutler.odyssey.lib.ovgdb.OvgdbManager
import com.codebutler.odyssey.lib.ovgdb.db.OvgdbDatabase
import com.codebutler.odyssey.lib.ovgdb.db.entity.Release
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.gojuno.koptional.rxjava2.filterSome
import com.gojuno.koptional.toOptional
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GameLibrary(
        private val odysseydb: OdysseyDatabase,
        private val ovgdbManager: OvgdbManager,
        private val libraryProviders: List<GameLibraryProvider>) {

    companion object {
        const val TAG = "OdysseyLibrary"
    }

    val games = odysseydb.gameDao().watchAll()

    fun indexGames() {
        addNewGames()
        removeDeletedGames()
    }

    private fun addNewGames() {
        ovgdbManager.dbReady
                .observeOn(Schedulers.io())
                .subscribe { ovgdb -> addNewGamesWithDb(ovgdb) }
    }

    private fun addNewGamesWithDb(ovgdb: OvgdbDatabase) {
        Observable.fromIterable(libraryProviders)
                .flatMapSingle { provider ->
                    provider.listFiles()
                }
                .flatMapIterable { it }
                .flatMapSingle { file ->
                    Log.d(TAG, "Got file: $file ${file.uri}")
                    odysseydb.gameDao().selectByFileUri(file.uri.toString())
                            .toSingleAsOptional()
                            .map { game -> Pair(file, game) }
                }
                .doOnNext { (file, game) -> Log.d(TAG, "Game already indexed? ${file.name} ${game is Some}") }
                .filter { (_, game) -> game is None }
                .map { (file, _) -> file }
                .flatMapSingle { file ->
                    when (file.crc) {
                        null -> Maybe.empty()
                        else -> ovgdb.romDao().findByCRC(file.crc)
                    }.switchIfEmpty(ovgdb.romDao().findByFileName(file.name))
                            .toSingleAsOptional()
                            .map { rom -> Pair(file, rom) }
                }
                .doOnNext { (file, rom) -> Log.d(TAG, "Rom Found: ${file.name} ${rom is Some}") }
                .flatMapSingle { (file, rom) ->
                    when (rom) {
                        is Some -> ovgdb.releaseDao().findByRomId(rom.value.id)
                                .toSingleAsOptional()
                        else -> Single.just<Optional<Release>>(None)
                    }.map { release -> Triple(file, rom, release) }
                }
                .doOnNext { (file, _, release) -> Log.d(TAG, "Release found: ${file.name}, ${release is Some}") }
                .flatMapSingle { (file, rom, release) ->
                    when (rom) {
                        is Some -> ovgdb.systemDao().findById(rom.value.systemId)
                                .toSingleAsOptional()
                        else -> Single.just(None)
                    }.map { ovgdbSystem -> Triple(file, release, ovgdbSystem) }
                }
                .doOnNext { (file, _, ovgdbSystem) -> Log.d(TAG, "OVGDB System Found: ${file.name}, ${ovgdbSystem is Some}") }
                .map { (file, release, ovgdbSystem) ->
                    var system = when (ovgdbSystem) {
                        is Some -> GameSystem.findByOeid(ovgdbSystem.value.oeid)
                        else -> null
                    }
                    if (system == null) {
                        Log.d(TAG, "System not found, trying file extension: ${file.name}")
                        system = GameSystem.findByFileExtension(file.extension)
                    }
                    if (system == null) {
                        Log.d(TAG, "Giving up on ${file.name}")
                    } else {
                        Log.d(TAG, "Found system!! $system")
                    }
                    Triple(file, release, system.toOptional())
                }
                .map { (file, release, system) ->
                    when (system) {
                        is Some -> Game(
                                fileName = file.name,
                                fileUri = file.uri,
                                title = release.toNullable()?.titleName ?: file.name,
                                systemId = system.value.id,
                                coverFrontUrl = release.toNullable()?.coverFront
                        ).toOptional()
                        else -> None
                    }
                }
                .filterSome()
                .subscribe(
                        { game ->
                            Log.d(TAG, "Insert: $game")
                            odysseydb.gameDao().insert(game)
                        },
                        { error -> Log.e(TAG, "Error while indexing", error) },
                        { Log.d(TAG, "Indexing complete") })
    }

    private fun removeDeletedGames() {
        odysseydb.gameDao().selectAll()
                .subscribeOn(Schedulers.io())
                .toObservable()
                .flatMapIterable { it }
                .switchMapSingle { game ->
                    val provider = getProvider(game.fileUri)
                    if (provider != null) {
                        provider.fileExists(game.fileUri)
                                .map { exists -> Pair(game, exists) }
                    } else {
                        Single.just(Pair(game, false))
                    }
                }
                .filter { (_, exists) -> exists.not() }
                .map { (game, _) -> game }
                .collectInto(mutableListOf<Game>(), { list, game -> list.add(game) })
                .subscribe { games ->
                    Log.d(TAG, "Removing games: $games")
                    odysseydb.gameDao().delete(games)
                }
    }

    private fun getProvider(uri: Uri)
            = libraryProviders.find { it.uriScheme == uri.scheme }
}
