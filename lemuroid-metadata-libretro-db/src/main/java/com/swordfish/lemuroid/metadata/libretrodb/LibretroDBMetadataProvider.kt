
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

// TODO We are not currently trying to guess the system using extensions. This is often not reliable, but sometimes it
// might work (think about .n64/.z64)

class LibretroDBMetadataProvider(private val ovgdbManager: LibretroDBManager) : GameMetadataProvider {
    override fun transformer(startedAtMs: Long) = ObservableTransformer<StorageFile, Optional<Game>> { upstream ->
        ovgdbManager.dbReady
                .flatMapObservable { db: LibretroDatabase ->
                    upstream
                        .doOnNext { Timber.d("Looking metadata for file: $it") }
                        .flatMapSingle { file ->
                            findByCRC(file, db)
                                .switchIfEmpty(findByName(db, file))
                                .doOnSuccess { Timber.d("Metadata retrieved for item: $it") }
                                .map { convertToGame(it, file, startedAtMs) }
                                .toSingle(None)
                                .doOnError { Timber.e("Error in retrieving $file metadata: $it... Skipping.") }
                                .onErrorReturn { None }
                        }
                }
    }

    private fun convertToGame(rom: LibretroRom, file: StorageFile, startedAtMs: Long): Optional<Game> {
        val system = GameSystem.findByShortName(rom.system!!)!!
        val game = Game(
                fileName = file.name,
                fileUri = file.uri,
                title = rom.name ?: file.name,
                systemId = system.id,
                developer = rom.developer,
                coverFrontUrl = computeCoverUrl(system, rom.name),
                lastIndexedAt = startedAtMs
        )
        return Some(game)
    }

    private fun findByName(db: LibretroDatabase, file: StorageFile) =
            db.gameDao().findByFileName(file.name)

    private fun findByCRC(file: StorageFile, db: LibretroDatabase): Maybe<LibretroRom> {
        return file.crc?.let { db.gameDao().findByCRC(it) } ?: Maybe.empty()
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
            else -> null
        }

        if (name == null || systemName == null) {
            return null
        }

        return "http://thumbnails.libretro.com/$systemName/Named_Boxarts/$name.png"
    }
}
