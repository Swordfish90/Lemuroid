package com.swordfish.lemuroid.lib.storage.local

import android.content.Context
import com.swordfish.lemuroid.common.kotlin.calculateCrc32
import com.swordfish.lemuroid.common.kotlin.toStringCRC32
import com.swordfish.lemuroid.lib.storage.BaseStorageFile
import com.swordfish.lemuroid.lib.storage.ISOScanner
import com.swordfish.lemuroid.lib.storage.StorageFile
import timber.log.Timber
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object DocumentFileParser {

    private const val MAX_CHECKED_ENTRIES = 3
    private const val SINGLE_ARCHIVE_THRESHOLD = 0.9
    private const val MAX_SIZE_CRC32 = 500_000_000

    fun parseDocumentFile(context: Context, baseStorageFile: BaseStorageFile): StorageFile {
        return if (baseStorageFile.extension == "zip") {
            Timber.d("Detected zip file. ${baseStorageFile.name}")
            parseZipFile(context, baseStorageFile)
        } else {
            Timber.d("Detected standard file. ${baseStorageFile.name}")
            parseStandardFile(context, baseStorageFile)
        }
    }

    private fun parseZipFile(context: Context, baseStorageFile: BaseStorageFile): StorageFile {
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

    private fun parseCompressedGame(baseStorageFile: BaseStorageFile, entry: ZipEntry, zipInputStream: ZipInputStream): StorageFile {
        Timber.d("Processing zipped entry: ${entry.name}")

        val serial = ISOScanner.extractSerial(entry.name, zipInputStream)

        return StorageFile(entry.name, entry.size, entry.crc.toStringCRC32(), serial, baseStorageFile.uri, baseStorageFile.uri.path)
    }

    private fun parseStandardFile(context: Context, baseStorageFile: BaseStorageFile): StorageFile {
        val crc32 = if (baseStorageFile.size < MAX_SIZE_CRC32) {
            context.contentResolver.openInputStream(baseStorageFile.uri)?.calculateCrc32()
        } else {
            null
        }

        val serial = context.contentResolver.openInputStream(baseStorageFile.uri)?.let { inputStream ->
            ISOScanner.extractSerial(baseStorageFile.name, inputStream)
        }

        Timber.d("Detected file name: ${baseStorageFile.name}, crc: $crc32")

        return StorageFile(baseStorageFile.name, baseStorageFile.size, crc32, serial, baseStorageFile.uri, baseStorageFile.uri.path)
    }

    /* Finds a zip entry which we assume is a game. Lemuroids only supports single archive games, so we are looking for
       an entry which occupies a large percentage of the archive space. This is very fast heuristic to compute and
       avoids reading the whole stream in most scenarios.*/
    fun findGameEntry(openedInputStream: ZipInputStream, fileSize: Long = -1): ZipEntry? {
        for (i in 0..MAX_CHECKED_ENTRIES) {
            val entry = openedInputStream.nextEntry ?: break
            if (!isGameEntry(entry, fileSize)) continue
            return entry
        }
        return null
    }

    private fun isGameEntry(entry: ZipEntry, fileSize: Long): Boolean {
        if (fileSize <= 0 || entry.compressedSize <= 0) return false
        return (entry.compressedSize.toFloat() / fileSize.toFloat()) > SINGLE_ARCHIVE_THRESHOLD
    }
}
