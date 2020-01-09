
package com.swordfish.lemuroid.metadata.libretrodb

import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDBManager
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDatabase
import com.swordfish.lemuroid.metadata.libretrodb.db.entity.LibretroRom
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.Some
import io.reactivex.Maybe
import io.reactivex.ObservableTransformer
import timber.log.Timber

class LibretroDBMetadataProvider(private val ovgdbManager: LibretroDBManager) : GameMetadataProvider {
    override fun transformer(startedAtMs: Long) = ObservableTransformer<StorageFile, Optional<Game>> { upstream ->
        ovgdbManager.dbReady
                .flatMapObservable { db: LibretroDatabase ->
                    upstream
                        .doOnNext { Timber.d("Looking metadata for file: $it") }
                        .flatMapSingle { file ->
                            findByCRC(file, db)
                                .switchIfEmpty(findByName(db, file))
                                .switchIfEmpty(findByUniqueExtension(file))
                                .doOnSuccess { Timber.d("Metadata retrieved for item: $it") }
                                .map { convertToGame(it, file, startedAtMs) }
                                .toSingle(None)
                                .doOnError { Timber.e("Error in retrieving $file metadata: $it... Skipping.") }
                                .onErrorReturn { None }
                        }
                }
    }

    private fun convertToGame(rom: GameMetadata, file: StorageFile, startedAtMs: Long): Optional<Game> {
        val system = GameSystem.findById(rom.system!!)

        val thumbnail = if (rom.includeThumbnail) {
            computeCoverUrl(system, rom.name)
        } else {
            null
        }

        val game = Game(
                fileName = file.name,
                fileUri = file.uri,
                title = rom.name ?: file.name,
                systemId = system.id,
                developer = rom.developer,
                coverFrontUrl = thumbnail,
                lastIndexedAt = startedAtMs
        )
        return Some(game)
    }

    private fun convertToGameMetadata(rom: LibretroRom): GameMetadata {
        return GameMetadata(
                name = rom.name,
                romName = rom.romName,
                includeThumbnail = true,
                system = rom.system,
                crc32 = rom.crc32,
                developer = rom.developer
        )
    }

    private fun findByName(db: LibretroDatabase, file: StorageFile): Maybe<GameMetadata> {
        return db.gameDao().findByFileName(file.name)
                .filter {
                    if (GameSystem.findById(it.system!!).requiresCRCMatch) {
                        it.crc32 == file.crc
                    } else {
                        true
                    }
                }
                .map { convertToGameMetadata(it) }
    }

    private fun findByCRC(file: StorageFile, db: LibretroDatabase): Maybe<GameMetadata> {
        return file.crc?.let { crc32 ->
            db.gameDao().findByCRC(crc32).map {
                convertToGameMetadata(it)
            }
        } ?: Maybe.empty()
    }

    private fun findByUniqueExtension(file: StorageFile) = Maybe.fromCallable {
        val system = GameSystem.findByFileExtension(file.extension)

        val result = system?.let {
            GameMetadata(
                    name = file.extensionlessName,
                    romName = file.name,
                    includeThumbnail = false,
                    system = it.id,
                    crc32 = file.crc,
                    developer = null
            )
        }

        result
    }

    private fun computeCoverUrl(system: GameSystem, name: String?): String? {
        val systemName = when (system.id) {
            GameSystem.GB_ID -> "Nintendo - Game Boy"
            GameSystem.GBC_ID -> "Nintendo - Game Boy Color"
            GameSystem.GBA_ID -> "Nintendo - Game Boy Advance"
            GameSystem.N64_ID -> "Nintendo - Nintendo 64"
            GameSystem.GENESIS_ID -> "Sega - Mega Drive - Genesis"
            GameSystem.NES_ID -> "Nintendo - Nintendo Entertainment System"
            GameSystem.SNES_ID -> "Nintendo - Super Nintendo Entertainment System"
            GameSystem.SMS_ID -> "Sega - Master System - Mark III"
            GameSystem.ARCADE_FB_NEO -> "FBNeo - Arcade Games"
            else -> null
        }

        if (name == null || systemName == null) {
            return null
        }

        val imageType = when (system.id) {
            GameSystem.ARCADE_FB_NEO -> "Named_Snaps"
            else -> "Named_Boxarts"
        }

        return "http://thumbnails.libretro.com/$systemName/$imageType/$name.png"
    }

    private data class GameMetadata(
        val name: String?,
        val system: String?,
        val romName: String?,
        val developer: String?,
        val crc32: String?,
        val includeThumbnail: Boolean
    )
}
