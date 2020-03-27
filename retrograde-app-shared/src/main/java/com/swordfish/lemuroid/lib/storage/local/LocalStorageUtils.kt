package com.swordfish.lemuroid.lib.storage.local

import android.content.Context
import android.net.Uri
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object LocalStorageUtils {

    private const val MAX_CHECKED_ENTRIES = 3
    private const val SINGLE_ARCHIVE_THRESHOLD = 0.9

    /* Finds a zip entry which we assume is a game. Lemuroids only supports single archive games, so we are looking for
       an entry which occupies a large percentage of the archive space. This is very fast euristic to compute and really
       speeds up reading the ZipInputStream.*/
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

    fun getCacheFileForGame(folderName: String, context: Context, game: Game): File {
        val gamesCachePath = buildPath(folderName, game.systemId)
        val gamesCacheDir = File(context.cacheDir, gamesCachePath)
        gamesCacheDir.mkdirs()
        return File(gamesCacheDir, game.fileName)
    }

    fun copyInputStreamToFile(inputFileStream: InputStream, gameFile: File) {
        inputFileStream.use { inputStream ->
            gameFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    fun extractZipEntryToFile(zipInputFileStream: ZipInputStream, entryName: String, gameFile: File) {
        zipInputFileStream.use { inputStream ->
            while (true) {
                val entry = inputStream.nextEntry
                if (entry.name == entryName) break
            }
            copyInputStreamToFile(inputStream, gameFile)
        }
    }

    private fun buildPath(vararg chunks: String): String {
        return chunks.joinToString(separator = File.separator)
    }
}
