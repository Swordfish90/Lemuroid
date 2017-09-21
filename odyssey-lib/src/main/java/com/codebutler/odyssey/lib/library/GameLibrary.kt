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

import android.util.Log
import com.codebutler.odyssey.common.Optional
import com.codebutler.odyssey.lib.db.OdysseyDatabase
import com.codebutler.odyssey.common.db.entity.Game
import com.codebutler.odyssey.lib.library.sdcard.LocalGameLibraryProvider
import com.codebutler.odyssey.lib.ovgdb.OvgdbManager
import com.codebutler.odyssey.lib.ovgdb.entity.Release
import com.codebutler.odyssey.lib.ovgdb.entity.Rom
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class GameLibrary(
        private val odysseydb: OdysseyDatabase,
        private val ovgdbManager: OvgdbManager) {

    companion object {
        const val TAG = "OdysseyLibrary"
    }

    private val providers = listOf(LocalGameLibraryProvider())

    val games = odysseydb.gameDao().getAll()

    fun indexGames() {
        ovgdbManager.dbReady
                .observeOn(Schedulers.io())
                .subscribe { ovgdb ->
                    odysseydb.gameDao().deleteAll()
                    Observable.fromIterable(providers)
                            .flatMapSingle { provider ->
                                provider.listItems()
                            }
                            .flatMap { files ->
                                Observable.fromIterable(files)
                            }
                            .flatMapMaybe { file ->
                                Log.d(TAG, "Got file: $file")
                                ovgdb.romDao().findByCRC(file.crc)
                                        .switchIfEmpty(ovgdb.romDao().findByFileName(file.name))
                                        .map { rom -> Optional(rom) }
                                        .switchIfEmpty(Maybe.just<Optional<Rom>>(Optional())
                                                .doOnSuccess { Log.d(TAG, "Rom not found ${file.name}") }
                                        )
                                        .map { rom -> Pair(file, rom) }
                            }
                            .flatMapMaybe { (file, rom) ->
                                val releaseOptionalMaybe = if (rom.isPresent) {
                                    ovgdb.releaseDao().findByRomId(rom.get.id)
                                            .map { release -> Optional(release) }
                                            .switchIfEmpty(Maybe.just<Optional<Release>>(Optional())
                                                    .doOnSuccess { Log.d(TAG, "Release not found: ${rom.get.id}") }
                                            )
                                } else {
                                    Maybe.just<Optional<Release>>(Optional())
                                }
                                releaseOptionalMaybe
                                        .map { release -> Game(
                                                fileName = file.name,
                                                fileUri = file.uri,
                                                title = release.getOrNull?.titleName ?: file.name,
                                                coverFrontUrl = release.getOrNull?.coverFront)
                                        }
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
}
