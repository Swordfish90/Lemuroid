
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
                                .switchIfEmpty(findByFilename(db, file))
                                .switchIfEmpty(findByPathAndFilename(db, file))
                                .switchIfEmpty(findByNameAndSupportedExtensions(db, file))
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
                systemId = system.id.dbname,
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

    private fun findByFilename(db: LibretroDatabase, file: StorageFile): Maybe<GameMetadata> {
        return db.gameDao().findByFileName(file.name)
                .filter { extractGameSystem(it).scanOptions.scanByFilename }
                .map { convertToGameMetadata(it) }
    }

    private fun findByNameAndSupportedExtensions(db: LibretroDatabase, file: StorageFile): Maybe<GameMetadata> {
        return db.gameDao().findByName("${file.extensionlessName}%")
                .flatMapMaybe { roms ->
                    Maybe.fromCallable {

                        // We really don't have anything to work on at this point. So we rely on cutting edge heuristics.
                        val compatibleRoms = roms
                            .filter { rom -> file.extension in extractGameSystem(rom).supportedExtensions }
                            .filter { rom -> areNamesCompatible(file.extensionlessName, rom.name!!) }
                            .sortedBy { rom -> rom.name?.length }

                        compatibleRoms.firstOrNull()
                    }
                }
                .filter { extractGameSystem(it).scanOptions.scanByNameAndSupportedExtensions }
                .map { convertToGameMetadata(it) }
    }

    private fun areNamesCompatible(name1: String, name2: String): Boolean {
        Timber.d("Checking name compatibility of $name1 and $name2")
        val sanitizedName1 = name1.replace("\\(.+?\\)".toRegex(), "").trim()
        val sanitizedName2 = name2.replace("\\(.+?\\)".toRegex(), "").trim()
        Timber.d("Sanitized names: $sanitizedName1, $sanitizedName2")
        return sanitizedName1 == sanitizedName2
    }

    private fun findByPathAndFilename(db: LibretroDatabase, file: StorageFile): Maybe<GameMetadata> {
        return db.gameDao().findByFileName(file.name)
            .filter { extractGameSystem(it).scanOptions.scanByPathAndFilename }
            .filter { file.path?.contains(extractGameSystem(it).id.dbname) == true }
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
        val system = GameSystem.findByUniqueFileExtension(file.extension)

        if (system?.scanOptions?.scanByUniqueExtension == false) {
            return@fromCallable null
        }

        val result = system?.let {
            GameMetadata(
                    name = file.extensionlessName,
                    romName = file.name,
                    includeThumbnail = false,
                    system = it.id.dbname,
                    crc32 = file.crc,
                    developer = null
            )
        }

        result
    }

    private fun extractGameSystem(rom: LibretroRom): GameSystem {
        return GameSystem.findById(rom.system!!)
    }

    private fun computeCoverUrl(system: GameSystem, name: String?): String? {
        val systemName = system.libretroFullName

        if (name == null) {
            return null
        }

        val imageType = "Named_Boxarts"

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
