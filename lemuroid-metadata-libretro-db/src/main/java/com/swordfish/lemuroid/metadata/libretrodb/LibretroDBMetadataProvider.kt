package com.swordfish.lemuroid.metadata.libretrodb

import androidx.paging.PagingSource
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.metadata.GameMetadata
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDBManager
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDatabase
import com.swordfish.lemuroid.metadata.libretrodb.db.entity.LibretroRom
import timber.log.Timber
import java.util.Locale

class LibretroDBMetadataProvider(private val libretroDbManager: LibretroDBManager) : GameMetadataProvider {
    private val artworkProvider = LibretroDBArtworkProvider(libretroDbManager)
    private val sortedSystemIds: List<String> by lazy {
        SystemID.entries
            .map { it.dbname }
            .sortedByDescending { it.length }
    }

    override suspend fun retrieveMetadata(storageFile: StorageFile): GameMetadata? {
        val db = libretroDbManager.dbInstance

        Timber.d("Looking metadata for file: $storageFile")

        val metadataResult = runCatching {
            findByCRC(storageFile, db)
                ?: findBySerial(storageFile, db)
                ?: findByFileName(db, storageFile)
                ?: findByGameName(db, storageFile)
                ?: findByPathAndFileName(db, storageFile)
                ?: findByUniqueExtension(storageFile)
                ?: findByKnownSystem(storageFile)
                ?: findByPathAndSupportedExtension(storageFile)
        }

        metadataResult.onFailure {
            Timber.e("Error in retrieving $storageFile metadata: $it... Skipping.")
        }

        val metadata = metadataResult.getOrNull() ?: return null

        return runCatching { artworkProvider.resolve(metadata) }
            .onFailure { Timber.e(it, "Error retrieving artwork for $storageFile") }
            .getOrElse { metadata }
    }

    fun searchGames(systemId: String, query: String): PagingSource<Int, LibretroRom> {
        val escaped = query.trim().replace("!", "!!").replace("%", "!%").replace("_", "!_")
        return libretroDbManager.dbInstance.gameDao().searchGamesWithArtwork(systemId, "%$escaped%")
    }

    fun convertToGameMetadata(rom: LibretroRom): GameMetadata {
        val artworkName = rom.artwork?.ifEmpty { rom.name }
        val thumbnail = artworkProvider.coverUrl(rom.system!!, artworkName)

        return GameMetadata(
            name = rom.name,
            romName = rom.romName,
            thumbnail = thumbnail,
            system = rom.system,
            developer = rom.developer,
        )
    }

    private fun findByPathAndSupportedExtension(file: StorageFile): GameMetadata? {
        val system =
            sortedSystemIds
                .filter { parentContainsSystem(file.path, it) }
                .map { GameSystem.findById(it) }
                .filter { it.scanOptions.scanByPathAndSupportedExtensions }
                .firstOrNull { it.supportedExtensions.contains(file.extension) }

        return system?.let {
            GameMetadata(
                name = file.extensionlessName,
                romName = file.name,
                thumbnail = null,
                system = it.id.dbname,
                developer = null,
            )
        }
    }

    private fun parentContainsSystem(
        parent: String?,
        dbname: String,
    ): Boolean {
        return parent?.lowercase(Locale.getDefault())?.contains(dbname) == true
    }

    private suspend fun findByCRC(
        file: StorageFile,
        db: LibretroDatabase,
    ): GameMetadata? {
        if (file.crc == null || file.crc == "0") return null
        return file.crc?.let { crc32 -> db.gameDao().findByCRC(crc32) }
            ?.let { convertToGameMetadata(it) }
    }

    private suspend fun findBySerial(
        file: StorageFile,
        db: LibretroDatabase,
    ): GameMetadata? {
        if (file.serial == null) return null
        return db.gameDao().findBySerial(file.serial!!)
            ?.let { convertToGameMetadata(it) }
    }

    private fun findByKnownSystem(file: StorageFile): GameMetadata? {
        if (file.systemID == null) return null

        return GameMetadata(
            name = file.extensionlessName,
            romName = file.name,
            thumbnail = null,
            system = file.systemID!!.dbname,
            developer = null,
        )
    }

    private fun findByUniqueExtension(file: StorageFile): GameMetadata? {
        val system = GameSystem.findByUniqueFileExtension(file.extension)

        if (system?.scanOptions?.scanByUniqueExtension == false) {
            return null
        }

        val result =
            system?.let {
                GameMetadata(
                    name = file.extensionlessName,
                    romName = file.name,
                    thumbnail = null,
                    system = it.id.dbname,
                    developer = null,
                )
            }

        return result
    }

    private fun extractGameSystem(rom: LibretroRom): GameSystem {
        return GameSystem.findById(rom.system!!)
    }

    private suspend fun findByFileName(
        db: LibretroDatabase,
        file: StorageFile,
    ): GameMetadata? {
        return db.gameDao().findByFileName(file.name)
            .firstOrNull { extractGameSystem(it).scanOptions.scanByFileName }
            ?.let { convertToGameMetadata(it) }
    }

    private suspend fun findByPathAndFileName(
        db: LibretroDatabase,
        file: StorageFile,
    ): GameMetadata? {
        return db.gameDao().findByFileName(file.name)
            .firstOrNull {
                val system = extractGameSystem(it)
                system.scanOptions.scanByPathAndFileName && parentContainsSystem(file.path, system.id.dbname)
            }
            ?.let { convertToGameMetadata(it) }
    }

    private suspend fun findByGameName(
        db: LibretroDatabase,
        file: StorageFile,
    ): GameMetadata? {
        return db.gameDao().findByGameName(file.extensionlessName)
            .firstOrNull {
                val system = extractGameSystem(it)
                val hasUniqueExtension = system.uniqueExtensions.any { extension ->
                    extension.equals(file.extension, ignoreCase = true)
                }
                system.scanOptions.scanByFileName && hasUniqueExtension
            }
            ?.let { convertToGameMetadata(it) }
    }
}
