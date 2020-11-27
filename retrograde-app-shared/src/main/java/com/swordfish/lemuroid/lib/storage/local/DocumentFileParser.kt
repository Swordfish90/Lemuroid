package com.swordfish.lemuroid.lib.storage.local

import android.content.Context
import com.swordfish.lemuroid.common.kotlin.calculateCrc32
import com.swordfish.lemuroid.common.kotlin.toStringCRC32
import com.swordfish.lemuroid.lib.storage.BaseStorageFile
import com.swordfish.lemuroid.lib.storage.ISOScanner
import com.swordfish.lemuroid.lib.storage.StorageFile
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object DocumentFileParser {

    private const val MAX_CHECKED_ENTRIES = 3
    private const val SINGLE_ARCHIVE_THRESHOLD = 0.9
    private const val MAX_SIZE_CRC32 = 1_000_000_000

    fun parseDocumentFile(context: Context, baseStorageFile: BaseStorageFile): StorageFile {
        return when (baseStorageFile.extension) {
            "zip" -> {
                Timber.d("Detected zip file. ${baseStorageFile.name}")
                parseZipFile(context, baseStorageFile)
            }

            "7z" -> {
                Timber.d("Detected 7z file. ${baseStorageFile.name}")
                parseSevenZFile(context, baseStorageFile)
            }

            else -> {
                Timber.d("Detected standard file. ${baseStorageFile.name}")
                parseStandardFile(context, baseStorageFile)
            }
        }
    }

    private fun parseSevenZFile(context: Context, baseStorageFile: BaseStorageFile): StorageFile {
        /* Apache Compress' 7z implementation does not supports IO streams, but only File.
         To create a SevenZFile from Android's Uri, we are bound to create a temp File.
         Another option is to extract the exact path name and create a SevenZFile,
         but this is quite inconsistent for different Android API versions.
         */
        val file = File.createTempFile("temp_seven_z_file_from_uri", ".7z", context.cacheDir)
        val inputStream = context.contentResolver.openInputStream(baseStorageFile.uri)
        inputStream?.let { file.copyInputStreamToFile(it) }
        val sevenZFile = SevenZFile(file)
        val gameEntry = findGameEntry(sevenZFile, baseStorageFile.size)
        return if (gameEntry != null) {
            Timber.d("Handing 7z file as compressed game: ${baseStorageFile.name}")
            val entryInputStream = kotlin.runCatching {
                sevenZFile.getInputStream(gameEntry)
            }.getOrNull()
            parseSevenZCompressedGame(
                baseStorageFile,
                gameEntry,
                entryInputStream
            ).also { sevenZFile.close() }
        } else {
            Timber.d("Handing 7z file as standard: ${baseStorageFile.name}")
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

    private fun parseCompressedGame(
        baseStorageFile: BaseStorageFile,
        entry: ZipEntry,
        zipInputStream: ZipInputStream
    ): StorageFile {
        Timber.d("Processing zipped entry: ${entry.name}")

        val serial = ISOScanner.extractSerial(entry.name, zipInputStream)

        return StorageFile(
            entry.name,
            entry.size,
            entry.crc.toStringCRC32(),
            serial,
            baseStorageFile.uri,
            baseStorageFile.uri.path
        )
    }

    private fun parseSevenZCompressedGame(
        baseStorageFile: BaseStorageFile,
        entry: SevenZArchiveEntry,
        inputStream: InputStream?
    ): StorageFile {
        Timber.d("Processing sevenZ entry: ${entry.name}")

        val serial = inputStream?.let { ISOScanner.extractSerial(entry.name, it) }

        return StorageFile(
            entry.name,
            entry.size,
            entry.crcValue.toStringCRC32(),
            serial,
            baseStorageFile.uri,
            baseStorageFile.uri.path
        )
    }

    private fun parseStandardFile(context: Context, baseStorageFile: BaseStorageFile): StorageFile {
        val serial = context.contentResolver.openInputStream(baseStorageFile.uri)
            ?.let { inputStream -> ISOScanner.extractSerial(baseStorageFile.name, inputStream) }

        val crc32 = if (baseStorageFile.size < MAX_SIZE_CRC32 && serial == null) {
            context.contentResolver.openInputStream(baseStorageFile.uri)?.calculateCrc32()
        } else {
            null
        }

        Timber.d("Detected file name: ${baseStorageFile.name}, crc: $crc32")

        return StorageFile(
            baseStorageFile.name,
            baseStorageFile.size,
            crc32,
            serial,
            baseStorageFile.uri,
            baseStorageFile.uri.path
        )
    }

    /* Finds a zip entry which we assume is a game. Lemuroids only supports single archive games,
       so we are looking for an entry which occupies a large percentage of the archive space.
       This is very fast heuristic to compute and avoids reading the whole stream in most
       scenarios.*/
    fun findGameEntry(openedInputStream: ZipInputStream, fileSize: Long = -1): ZipEntry? {
        for (i in 0..MAX_CHECKED_ENTRIES) {
            val entry = openedInputStream.nextEntry ?: break
            if (!isGameEntry(entry, fileSize)) continue
            return entry
        }
        return null
    }

    /* Finds a sevenZ entry which we assume is a game. Lemuroids only supports single archive games,
       so we are looking for an entry which occupies a large percentage of the archive space.
       This is very fast heuristic to compute and avoids reading the whole stream in most
       scenarios.*/
    fun findGameEntry(sevenZFile: SevenZFile, fileSize: Long = -1): SevenZArchiveEntry? {
        for (i in 0..MAX_CHECKED_ENTRIES) {
            val entry = sevenZFile.nextEntry ?: break
            if (!isGameEntry(entry, fileSize)) continue
            return entry
        }
        sevenZFile.close()
        return null
    }

    private fun isGameEntry(entry: ZipEntry, fileSize: Long): Boolean {
        if (fileSize <= 0 || entry.compressedSize <= 0) return false
        return (entry.compressedSize.toFloat() / fileSize.toFloat()) > SINGLE_ARCHIVE_THRESHOLD
    }

    private fun isGameEntry(entry: SevenZArchiveEntry, fileSize: Long): Boolean {
        if (fileSize <= 0 || entry.size <= 0) return false
        return (entry.size.toFloat() / fileSize.toFloat()) > SINGLE_ARCHIVE_THRESHOLD
    }

    private fun File.copyInputStreamToFile(inputStream: InputStream) {
        val buffer = ByteArray(1024)

        inputStream.use { input ->
            this.outputStream().use { fileOut ->
                while (true) {
                    val length = input.read(buffer)
                    if (length <= 0)
                        break
                    fileOut.write(buffer, 0, length)
                }
                fileOut.flush()
            }
        }
    }
}
