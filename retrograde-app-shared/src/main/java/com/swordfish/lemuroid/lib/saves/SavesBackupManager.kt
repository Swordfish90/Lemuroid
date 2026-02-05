package com.swordfish.lemuroid.lib.saves

import android.content.ContentResolver
import android.net.Uri
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class SavesBackupManager(
    private val directoriesManager: DirectoriesManager
) {
    sealed class BackupResult {
        object Success : BackupResult()
        data class Error(val message: String) : BackupResult()
    }

    suspend fun exportSaves(
        contentResolver: ContentResolver,
        destinationUri: Uri
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zipOut ->
                    val savesDir = directoriesManager.getSavesDirectory()
                    addDirectoryToZip(zipOut, savesDir, "saves")

                    val statesDir = directoriesManager.getStatesDirectory()
                    addDirectoryToZip(zipOut, statesDir, "states")

                    val previewsDir = directoriesManager.getStatesPreviewDirectory()
                    addDirectoryToZip(zipOut, previewsDir, "state-previews")
                }
            } ?: return@withContext BackupResult.Error("Cannot open output stream")
            BackupResult.Success
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun importSaves(
        contentResolver: ContentResolver,
        sourceUri: Uri
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry: ZipEntry? = zipIn.nextEntry
                    while (entry != null) {
                        val targetFile = resolveEntryToFile(entry.name)
                        if (targetFile != null && !entry.isDirectory) {
                            targetFile.parentFile?.mkdirs()
                            targetFile.outputStream().use { fileOut ->
                                zipIn.copyTo(fileOut)
                            }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            } ?: return@withContext BackupResult.Error("Cannot open input stream")
            BackupResult.Success
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun addDirectoryToZip(
        zipOut: ZipOutputStream,
        sourceDir: File,
        basePath: String
    ) {
        if (!sourceDir.exists()) return

        sourceDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                val relativePath = "$basePath/${file.relativeTo(sourceDir).path}"
                zipOut.putNextEntry(ZipEntry(relativePath))
                file.inputStream().use { it.copyTo(zipOut) }
                zipOut.closeEntry()
            }
        }
    }

    private fun resolveEntryToFile(entryName: String): File? {
        val baseDir = directoriesManager.getSavesDirectory().parentFile ?: return null

        val normalizedPath = entryName.replace("\\", "/")
        if (normalizedPath.contains("..")) return null

        return when {
            normalizedPath.startsWith("saves/") -> File(baseDir, normalizedPath)
            normalizedPath.startsWith("states/") -> File(baseDir, normalizedPath)
            normalizedPath.startsWith("state-previews/") -> File(baseDir, normalizedPath)
            else -> null
        }
    }
}
