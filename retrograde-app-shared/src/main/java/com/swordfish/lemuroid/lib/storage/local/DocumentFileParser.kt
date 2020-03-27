package com.swordfish.lemuroid.lib.storage.local

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.swordfish.lemuroid.common.kotlin.calculateCrc32
import com.swordfish.lemuroid.common.kotlin.isZipped
import com.swordfish.lemuroid.common.kotlin.toStringCRC32
import com.swordfish.lemuroid.lib.storage.ISOScanner
import com.swordfish.lemuroid.lib.storage.StorageFile
import timber.log.Timber
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object DocumentFileParser {

    private const val MAX_CHECKED_ENTRIES = 3
    private const val SINGLE_ARCHIVE_THRESHOLD = 0.9
    private const val MAX_SIZE_CRC32 = 500_000_000

    fun parseDocumentFile(context: Context, file: DocumentFile): StorageFile {
        return if (file.isZipped()) {
            Timber.d("Detected zip file. ${file.name}")
            parseZipFile(context, file)
        } else {
            Timber.d("Detected standard file. ${file.name}")
            parseStandardFile(context, file)
        }
    }

    private fun parseZipFile(context: Context, file: DocumentFile): StorageFile {
        val inputStream = context.contentResolver.openInputStream(file.uri)
        return ZipInputStream(inputStream).use {
            val gameEntry = findGameEntry(it, file.length())
            if (gameEntry != null) {
                Timber.d("Handing zip file as compressed game: ${file.name}")
                parseCompressedGame(file, gameEntry, it)
            } else {
                Timber.d("Handing zip file as standard: ${file.name}")
                parseStandardFile(context, file)
            }
        }
    }

    private fun parseCompressedGame(file: DocumentFile, entry: ZipEntry, zipInputStream: ZipInputStream): StorageFile {
        Timber.d("Processing zipped entry: ${entry.name}")

        val serial = ISOScanner.extractSerial(entry.name, zipInputStream)

        return StorageFile(entry.name, entry.size, entry.crc.toStringCRC32(), serial, file.uri, file.parentFile?.name)
    }

    private fun parseStandardFile(context: Context, file: DocumentFile): StorageFile {
        val crc32 = if (file.length() < MAX_SIZE_CRC32) {
            context.contentResolver.openInputStream(file.uri)?.calculateCrc32()
        } else {
            null
        }

        val serial = context.contentResolver.openInputStream(file.uri)?.let { inputStream ->
            ISOScanner.extractSerial(file.name!!, inputStream)
        }

        Timber.d("Detected file name: ${file.name}, crc: $crc32")

        return StorageFile(file.name!!, file.length(), crc32, serial, file.uri, file.parentFile?.name)
    }

    /* Finds a zip entry which we assume is a game. Lemuroids only supports single archive games, so we are looking for
       an entry which occupies a large percentage of the archive space. This is very fast heuristic to compute and
       avoids reading the whole stream in most scenarios.*/
    fun findGameEntry(openedInputStream: ZipInputStream, fileSize: Long = -1): ZipEntry? {
        for(i in 0..MAX_CHECKED_ENTRIES) {
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
