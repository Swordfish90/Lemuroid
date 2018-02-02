/*
 * OvgdbMetadataProvider.kt
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

package com.codebutler.retrograde.lib.ovgdb.db

import com.codebutler.retrograde.common.rx.toSingleAsOptional
import com.codebutler.retrograde.lib.library.GameSystem
import com.codebutler.retrograde.lib.library.db.entity.Game
import com.codebutler.retrograde.lib.library.metadata.GameMetadataProvider
import com.codebutler.retrograde.lib.ovgdb.db.entity.OvgdbRelease
import com.codebutler.retrograde.lib.storage.StorageFile
import com.codebutler.retrograde.metadata.ovgdb.db.OvgdbDatabase
import com.codebutler.retrograde.metadata.ovgdb.db.OvgdbManager
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import com.gojuno.koptional.toOptional
import io.reactivex.Maybe
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import timber.log.Timber

class OvgdbMetadataProvider(private val ovgdbManager: OvgdbManager) : GameMetadataProvider {
    override fun transformer(startedAtMs: Long) = ObservableTransformer<StorageFile, Optional<Game>> { upstream ->
        ovgdbManager.dbReady
                .flatMapObservable { ovgdb: OvgdbDatabase -> upstream
                        .flatMapSingle { file ->
                            when (file.crc) {
                                null -> Maybe.empty()
                                else -> ovgdb.romDao().findByCRC(file.crc!!)
                            }.switchIfEmpty(ovgdb.romDao().findByFileName(sanitizeRomFileName(file.name)))
                                    .toSingleAsOptional()
                                    .map { rom -> Pair(file, rom) }
                        }
                        .doOnNext { (file, rom) ->
                            Timber.d("Rom Found: ${file.name} ${rom is Some}")
                        }
                        .flatMapSingle { (file, rom) ->
                            when (rom) {
                                is Some -> ovgdb.releaseDao().findByRomId(rom.value.id)
                                        .toSingleAsOptional()
                                else -> Single.just<Optional<OvgdbRelease>>(None)
                            }.map { release -> Triple(file, rom, release) }
                        }
                        .doOnNext { (file, _, release) ->
                            Timber.d("Release found: ${file.name}, ${release is Some}")
                        }
                        .flatMapSingle { (file, rom, release) ->
                            when (rom) {
                                is Some -> ovgdb.systemDao().findById(rom.value.systemId)
                                        .toSingleAsOptional()
                                else -> Single.just(None)
                            }.map { ovgdbSystem -> Triple(file, release, ovgdbSystem) }
                        }
                        .doOnNext { (file, _, ovgdbSystem) ->
                            Timber.d("OVGDB System Found: ${file.name}, ${ovgdbSystem is Some}")
                        }
                        .map { (file, release, ovgdbSystem) ->
                            var system = when (ovgdbSystem) {
                                is Some -> {
                                    val gs = GameSystem.findByShortName(ovgdbSystem.value.shortName)
                                    if (gs == null) {
                                        Timber.e("System '${ovgdbSystem.value.shortName}' not found")
                                    }
                                    gs
                                }
                                else -> null
                            }
                            if (system == null) {
                                Timber.d("System not found, trying file extension: ${file.name}")
                                system = GameSystem.findByFileExtension(file.extension)
                            }
                            if (system == null) {
                                Timber.d("Giving up on ${file.name}")
                            } else {
                                Timber.d("Found system!! $system")
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
                                        developer = release.toNullable()?.developer,
                                        coverFrontUrl = release.toNullable()?.coverFront,
                                        lastIndexedAt = startedAtMs
                                ).toOptional()
                                else -> None
                            }
                        }
                }
    }

    private fun sanitizeRomFileName(fileName: String): String {
        return fileName
                .replace("(U)", "(USA)")
                .replace("(J)", "(Japan)")
                .replace(" [!]", "")
                .replace(Regex("\\.v64$"), ".n64")
                .replace(Regex("\\.z64$"), ".n64")
    }
}
