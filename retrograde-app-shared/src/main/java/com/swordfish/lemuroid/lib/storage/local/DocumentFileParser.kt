/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.lib.storage.local

import android.content.Context
import com.swordfish.lemuroid.common.kotlin.calculateCrc32
import com.swordfish.lemuroid.common.kotlin.get7zEntries
import com.swordfish.lemuroid.common.kotlin.toStringCRC32
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.storage.BaseStorageFile
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.lib.storage.scanner.SerialScanner
import timber.log.Timber
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object DocumentFileParser {
    private const val MAX_CHECKED_ENTRIES = 3
    private const val SINGLE_ARCHIVE_THRESHOLD = 0.9
    private const val MAX_SIZE_CRC32 = 1_000_000_000

    fun parseDocumentFile(
        context: Context,
        baseStorageFile: BaseStorageFile,
    ): StorageFile {
        return when (baseStorageFile.extension.lowercase()) {
            "zip" -> {
                Timber.d("Detected zip file. ${baseStorageFile.name}")
                parseZipFile(context, baseStorageFile)
            }

            "7z" -> {
                Timber.d("Detected 7z file. ${baseStorageFile.name}")
                parse7zFile(context, baseStorageFile)
            }

            else -> {
                Timber.d("Detected standard file. ${baseStorageFile.name}")
                parseStandardFile(context, baseStorageFile)
            }
        }
    }

    private fun parseZipFile(
        context: Context,
        baseStorageFile: BaseStorageFile,
    ): StorageFile {
        val inputStream = context.contentResolver.openInputStream(baseStorageFile.uri)
        return ZipInputStream(inputStream).use {
            val gameEntry = findGameEntry(it, baseStorageFile.size)
            if (gameEntry != null) {
                Timber.d("Handing zip file as compressed game: ${baseStorageFile.name}")
                parseCompressedGame(baseStorageFile, gameEntry, it)
            } else {
                Timber.d("Handing zip file as standard: ${baseStorageFile.name}")
                parseStandardFile(context, baseStorageFile)
            }
        }
    }

    private fun parse7zFile(
        context: Context,
        baseStorageFile: BaseStorageFile,
    ): StorageFile {
        // Copy to temp file for 7-Zip-JBinding (needs RandomAccessFile)
        val tempFile = File.createTempFile("7z_scan_", ".7z", context.cacheDir)
        try {
            context.contentResolver.openInputStream(baseStorageFile.uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Use native 7-Zip-JBinding to read entries (much more memory efficient)
            val entries = tempFile.get7zEntries()

            // Find the game entry (first non-directory with reasonable size)
            val gameEntry = entries
                .filter { !it.isDirectory && it.size > 0 }
                .maxByOrNull { it.size }

            if (gameEntry != null) {
                val systemId = detectSystemFromFileName(gameEntry.name)
                val crc = gameEntry.crc?.toStringCRC32()

                return StorageFile(
                    gameEntry.name,
                    gameEntry.size,
                    crc,
                    null, // Serial requires decompression
                    baseStorageFile.uri,
                    baseStorageFile.uri.path,
                    systemId,
                )
            } else {
                return parse7zFallback(baseStorageFile)
            }
        } catch (e: Throwable) {
            Timber.e(e, "Error parsing 7z: ${baseStorageFile.name}")
            return parse7zFallback(baseStorageFile)
        } finally {
            tempFile.delete()
        }
    }

    private fun parse7zFallback(baseStorageFile: BaseStorageFile): StorageFile {
        val archiveName = baseStorageFile.name
        val gameFileName = archiveName.removeSuffix(".7z")
        val systemId = detectSystemFromFileName(gameFileName)

        val innerFileName = when {
            gameFileName.contains('.') -> gameFileName
            systemId != null -> "$gameFileName.${getExtensionForSystem(systemId)}"
            else -> gameFileName
        }

        return StorageFile(
            innerFileName,
            baseStorageFile.size,
            null,
            null,
            baseStorageFile.uri,
            baseStorageFile.uri.path,
            systemId,
        )
    }

    private fun detectSystemFromFileName(fileName: String): SystemID? {
        // Check if filename contains a known extension
        val lowerName = fileName.lowercase()
        return when {
            lowerName.endsWith(".3ds") || lowerName.endsWith(".cci") -> SystemID.NINTENDO_3DS
            lowerName.endsWith(".nds") -> SystemID.NDS
            lowerName.endsWith(".gba") -> SystemID.GBA
            lowerName.endsWith(".gbc") -> SystemID.GBC
            lowerName.endsWith(".gb") -> SystemID.GB
            lowerName.endsWith(".nes") -> SystemID.NES
            lowerName.endsWith(".snes") || lowerName.endsWith(".sfc") -> SystemID.SNES
            lowerName.endsWith(".n64") || lowerName.endsWith(".z64") || lowerName.endsWith(".v64") -> SystemID.N64
            lowerName.endsWith(".iso") || lowerName.endsWith(".cso") -> SystemID.PSP
            lowerName.endsWith(".pbp") -> SystemID.PSP
            lowerName.endsWith(".bin") || lowerName.endsWith(".cue") -> SystemID.PSX
            lowerName.endsWith(".md") || lowerName.endsWith(".gen") || lowerName.endsWith(".smd") -> SystemID.GENESIS
            // Heuristics based on common naming patterns
            lowerName.contains("(3ds)") || lowerName.contains("[3ds]") -> SystemID.NINTENDO_3DS
            lowerName.contains("(nds)") || lowerName.contains("[nds]") -> SystemID.NDS
            lowerName.contains("(gba)") || lowerName.contains("[gba]") -> SystemID.GBA
            lowerName.contains("(psp)") || lowerName.contains("[psp]") -> SystemID.PSP
            lowerName.contains("(psx)") || lowerName.contains("[psx]") || lowerName.contains("(ps1)") -> SystemID.PSX
            else -> null
        }
    }

    private fun getExtensionForSystem(systemId: SystemID): String {
        return when (systemId) {
            SystemID.NINTENDO_3DS -> "3ds"
            SystemID.NDS -> "nds"
            SystemID.GBA -> "gba"
            SystemID.GBC -> "gbc"
            SystemID.GB -> "gb"
            SystemID.NES -> "nes"
            SystemID.SNES -> "sfc"
            SystemID.N64 -> "n64"
            SystemID.PSP -> "iso"
            SystemID.PSX -> "bin"
            SystemID.GENESIS -> "md"
            else -> "bin"
        }
    }

    private fun parseCompressedGame(
        baseStorageFile: BaseStorageFile,
        entry: ZipEntry,
        zipInputStream: ZipInputStream,
    ): StorageFile {
        Timber.d("Processing zipped entry: ${entry.name}")

        val diskInfo = SerialScanner.extractInfo(entry.name, zipInputStream)

        return StorageFile(
            entry.name,
            entry.size,
            entry.crc.toStringCRC32(),
            diskInfo.serial,
            baseStorageFile.uri,
            baseStorageFile.uri.path,
            diskInfo.systemID,
        )
    }

    private fun parseStandardFile(
        context: Context,
        baseStorageFile: BaseStorageFile,
    ): StorageFile {
        val diskInfo =
            context.contentResolver.openInputStream(baseStorageFile.uri)
                ?.let { inputStream -> SerialScanner.extractInfo(baseStorageFile.name, inputStream) }

        val crc32 =
            if (baseStorageFile.size < MAX_SIZE_CRC32 && diskInfo?.serial == null) {
                context.contentResolver.openInputStream(baseStorageFile.uri)?.calculateCrc32()
            } else {
                null
            }

        Timber.d("Parsed standard file: $baseStorageFile")

        return StorageFile(
            baseStorageFile.name,
            baseStorageFile.size,
            crc32,
            diskInfo?.serial,
            baseStorageFile.uri,
            baseStorageFile.uri.path,
            diskInfo?.systemID,
        )
    }

    /* Finds a zip entry which we assume is a game. Lemuroid only supports single archive games,
       so we are looking for an entry which occupies a large percentage of the archive space.
       This is very fast heuristic to compute and avoids reading the whole stream in most
       scenarios.*/
    fun findGameEntry(
        openedInputStream: ZipInputStream,
        fileSize: Long = -1,
    ): ZipEntry? {
        for (i in 0..MAX_CHECKED_ENTRIES) {
            val entry = openedInputStream.nextEntry ?: break
            if (!isGameEntry(entry, fileSize)) continue
            return entry
        }
        return null
    }

    private fun isGameEntry(
        entry: ZipEntry,
        fileSize: Long,
    ): Boolean {
        if (fileSize <= 0 || entry.compressedSize <= 0) return false
        return (entry.compressedSize.toFloat() / fileSize.toFloat()) > SINGLE_ARCHIVE_THRESHOLD
    }
}
