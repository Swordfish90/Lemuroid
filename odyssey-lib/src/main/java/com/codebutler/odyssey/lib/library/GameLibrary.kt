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

import android.arch.persistence.room.EmptyResultSetException
import android.net.Uri
import android.util.Log
import com.codebutler.odyssey.common.Optional
import com.codebutler.odyssey.common.filterAndGetOptional
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.library.provider.local.LocalGameLibraryProvider
import com.codebutler.odyssey.lib.ovgdb.OvgdbManager
import com.codebutler.odyssey.lib.ovgdb.db.entity.Release
import com.codebutler.odyssey.lib.ovgdb.db.entity.Rom
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GameLibrary(
        private val odysseydb: OdysseyDatabase,
        private val ovgdbManager: OvgdbManager) {

    companion object {
        const val TAG = "OdysseyLibrary"
    }

    private val providers = listOf(LocalGameLibraryProvider())

    val games = odysseydb.gameDao().watchAll()

    fun indexGames() {
        addNewGames()
        removeDeletedGames()
    }

    private fun addNewGames() {
        ovgdbManager.dbReady
                .observeOn(Schedulers.io())
                .subscribe { ovgdb ->
                    Observable.fromIterable(providers)
                            .flatMapSingle { provider ->
                                provider.listFiles()
                            }
                            .flatMap { files ->
                                Observable.fromIterable(files)
                            }
                            .flatMapMaybe { file ->
                                Log.d(TAG, "Got file: $file ${file.uri}")
                                odysseydb.gameDao().selectByFileUri(file.uri.toString())
                                        .map { Optional(it) }
                                        .switchIfEmpty(Maybe.just(Optional()))
                                        .map { game -> Pair(file, game) }
                            }
                            .doOnNext{ (file, game) -> Log.d(TAG, "Game found: ${file.name} ${game.isPresent}") }
                            .filter { (_, game) -> !game.isPresent }
                            .map { (file, _) -> file }
                            .flatMapMaybe { file ->
                                ovgdb.romDao().findByCRC(file.crc)
                                        .switchIfEmpty(ovgdb.romDao().findByFileName(file.name))
                                        .map { rom -> Optional(rom) }
                                        .switchIfEmpty(Maybe.just<Optional<Rom>>(Optional())
                                                .doOnSuccess { Log.d(TAG, "Rom not found ${file.name}") }
                                        )
                                        .map { rom -> Pair(file, rom) }
                            }
                            .flatMapMaybe { (file, rom) ->
                                val releaseMaybe = if (rom.isPresent) {
                                    ovgdb.releaseDao().findByRomId(rom.get.id)
                                            .map { release -> Optional(release) }
                                            .switchIfEmpty(Maybe.just<Optional<Release>>(Optional())
                                                    .doOnSuccess { Log.d(TAG, "Release not found: ${rom.get.id}") }
                                            )
                                } else {
                                    Maybe.just<Optional<Release>>(Optional())
                                }
                                releaseMaybe
                                        .map { release -> Triple(file, rom, release) }
                            }
                            .flatMapMaybe { (file, rom, release) ->
                                val systemSingle = if (rom.isPresent) {
                                    ovgdb.systemDao().findById(rom.get.systemId)
                                } else {
                                    Single.error(EmptyResultSetException(""))
                                }
                                systemSingle
                                        .map { ovgdbSystem ->
                                            val system = GameSystem.findByOeid(ovgdbSystem.oeid)
                                                    ?: throw EmptyResultSetException("")
                                            Log.d(TAG, "Found system!! $system")
                                            Optional(system)
                                        }
                                        .onErrorResumeNext { ex ->
                                            if (ex is EmptyResultSetException) {
                                                Log.d(TAG, "System not found, trying file extension: ${file.name}")
                                                val system = GameSystem.findByFileExtension(file.extension)
                                                if (system == null) {
                                                    Log.d(TAG, "Giving up on ${file.name}")
                                                }
                                                Single.just(Optional(system))
                                            } else {
                                                throw ex
                                            }
                                        }
                                        .filterAndGetOptional()
                                        .map { system -> Triple(file, release, system) }
                            }
                            .map { (file, release, system) -> Game(
                                    fileName = file.name,
                                    fileUri = file.uri,
                                    title = release.getOrNull?.titleName ?: file.name,
                                    systemId = system.id,
                                    coverFrontUrl = release.getOrNull?.coverFront)
                            }
                            .subscribe(
                                    { game ->
                                        Log.d(TAG, "Insert: $game")
                                        odysseydb.gameDao().insert(game)
                                    },
                                    { error -> Log.e(TAG, "Error while indexing", error) },
                                    { Log.d(TAG, "Indexing complete") })
                }
    }

    private fun removeDeletedGames() {
        odysseydb.gameDao().selectAll()
                .subscribeOn(Schedulers.io())
                .toObservable()
                .switchMap { Observable.fromIterable(it) }
                .filter { game -> getProvider(game.fileUri)?.fileExists(game.fileUri)?.not() ?: true }
                .collectInto(mutableListOf<Game>(), { list, game -> list.add(game) })
                .subscribe { games ->
                    Log.d(TAG, "Removing games: $games")
                    odysseydb.gameDao().delete(games)
                }

    }

    private fun getProvider(uri: Uri)
            = providers.find { it.uriScheme == uri.scheme }
}
