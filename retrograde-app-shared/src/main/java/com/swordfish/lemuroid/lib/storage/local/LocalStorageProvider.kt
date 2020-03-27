/*
 * LocalGameLibraryProvider.kt
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

package com.swordfish.lemuroid.lib.storage.local

import android.content.Context
import android.net.Uri
import androidx.leanback.preference.LeanbackPreferenceFragment
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.common.kotlin.calculateCrc32
import com.swordfish.lemuroid.common.kotlin.toStringCRC32
import com.swordfish.lemuroid.lib.R
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.storage.ISOScanner
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.StorageProvider
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class LocalStorageProvider(
    private val context: Context,
    private val directoriesManager: DirectoriesManager,
    override val metadataProvider: GameMetadataProvider
) : StorageProvider {

    override val id: String = "local"

    override val name: String = context.getString(R.string.local_storage)

    override val uriSchemes = listOf("file")

    override val prefsFragmentClass: Class<LeanbackPreferenceFragment>? = null

    override val enabledByDefault = true

    override fun listFiles(): Observable<StorageFile> =
        Observable.fromIterable(walkDirectory(getExternalFolder() ?: directoriesManager.getInternalRomsDirectory()))

    private fun getExternalFolder(): File? {
        val prefString = context.getString(R.string.pref_key_legacy_external_folder)
        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)
        return preferenceManager.getString(prefString, null)?.let { File(it) }
    }

    private fun walkDirectory(directory: File): Iterable<StorageFile> {
        return directory.walk()
            .filter { file -> file.isFile && !file.name.startsWith(".") }
            .map { handleFile(it) }
            .filterNotNull()
            .asIterable()
    }

    private fun handleFile(file: File): StorageFile? {
        return if (isZipped(file)) {
            Timber.d("Detected zip file. ${file.name}")
            handleFileAsZipFile(file)
        } else {
            Timber.d("Detected standard file. ${file.name}")
            handleFileAsStandardFile(file)
        }
    }

    private fun handleFileAsZipFile(file: File): StorageFile? {
        val inputStream = file.inputStream()
        return ZipInputStream(inputStream).use {
            val gameEntry = LocalStorageUtils.findGameEntry(it, file.length())
            if (gameEntry != null) {
                Timber.d("Handing zip file as compressed game: ${file.name}")
                handleFileAsCompressedGame(file, gameEntry, it)
            } else {
                Timber.d("Handing zip file as standard: ${file.name}")
                handleFileAsStandardFile(file)
            }
        }
    }

    private fun handleFileAsCompressedGame(file: File, entry: ZipEntry, zipInputStream: ZipInputStream): StorageFile {
        Timber.d("Processing zipped entry: ${entry.name}")

        val serial = ISOScanner.extractSerial(entry.name, zipInputStream)

        return StorageFile(entry.name, entry.size, entry.crc.toStringCRC32(), serial, Uri.fromFile(file), file.parent)
    }

    private fun handleFileAsStandardFile(file: File): StorageFile {
        return StorageFile(
            name = file.name,
            size = file.length(),
            crc = file.calculateCrc32(),
            uri = Uri.parse(file.toURI().toString()),
            parentFolder = file.parent,
            serial = ISOScanner.extractSerial(file.name, FileInputStream(file))
        )
    }

    override fun getGameRom(game: Game): Single<File> = Single.fromCallable {
        val originalFile = File(game.fileUri.path)
        if (!isZipped(originalFile) || originalFile.name == game.fileName) {
            return@fromCallable originalFile
        }

        val cacheFile = LocalStorageUtils.getCacheFileForGame(LOCAL_STORAGE_CACHE_SUBFOLDER, context, game)
        if (cacheFile.exists()) {
            return@fromCallable cacheFile
        }

        if (isZipped(originalFile)) {
            val stream = ZipInputStream(originalFile.inputStream())
            LocalStorageUtils.extractZipEntryToFile(stream, game.fileName, cacheFile)
        }

        cacheFile
    }

    private fun isZipped(file: File) = file.extension == "zip"

    companion object {
        const val LOCAL_STORAGE_CACHE_SUBFOLDER = "local-storage-games"
    }
}
