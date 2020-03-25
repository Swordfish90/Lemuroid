package com.swordfish.lemuroid.lib.storage.local

import android.content.Context
import com.swordfish.lemuroid.common.files.FileUtils
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object LocalStorageUtils {

    fun findFirstGameEntry(inputStream: ZipInputStream): ZipEntry? {
        val supportedExtensions = GameSystem.getSupportedExtensions()
        while (true) {
            val entry = inputStream.nextEntry ?: break
            if (FileUtils.extractExtension(entry.name) !in supportedExtensions) continue
            return entry
        }
        return null
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

    fun isSingleArchive(zipInputStream: ZipInputStream): Boolean {
        zipInputStream.use { inputStream ->
            findFirstGameEntry(inputStream)
            return inputStream.available() > 0
        }
    }

    fun extractFirstGameFromZipInputStream(zipInputFileStream: ZipInputStream, gameFile: File) {
        zipInputFileStream.use { inputStream ->
            findFirstGameEntry(inputStream)
            copyInputStreamToFile(inputStream, gameFile)
        }
    }

    private fun buildPath(vararg chunks: String): String {
        return chunks.joinToString(separator = File.separator)
    }
}
