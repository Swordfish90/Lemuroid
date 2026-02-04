package com.swordfish.lemuroid.lib.storage

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

import com.swordfish.lemuroid.lib.injection.PerApp
import javax.inject.Inject

@PerApp
class SaveStorageManager @Inject constructor(
    private val appContext: Context,
    private val directoriesManager: DirectoriesManager,
) {
    private val customSavesDirectoryUri: Uri?
        get() = directoriesManager.getCustomSavesDirectoryUri()?.let { Uri.parse(it) }

    private val defaultSavesDirectory: File
        get() = directoriesManager.getSavesDirectory()

    private val defaultStatesDirectory: File
        get() = directoriesManager.getStatesDirectory()

    suspend fun syncToCustomDirectory() = withContext(Dispatchers.IO) {
        val customUri = customSavesDirectoryUri ?: return@withContext
        val customDocumentFile = DocumentFile.fromTreeUri(appContext, customUri) ?: return@withContext

        val contentResolver = appContext.contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        try {
            contentResolver.takePersistableUriPermission(customUri, takeFlags)
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to take persistable URI permission for: %s", customUri)
            return@withContext
        }

        defaultSavesDirectory.walkTopDown().filter { it.isFile }.forEach { file ->
            val relativePath = "saves/${file.toRelativeString(defaultSavesDirectory)}"
            val targetFile = findOrCreateDocumentFile(customDocumentFile, relativePath)
            if (targetFile != null && targetFile.isFile) {
                appContext.contentResolver.openOutputStream(targetFile.uri)?.use { outputStream ->
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }

        defaultStatesDirectory.walkTopDown().filter { it.isFile }.forEach { file ->
            val relativePath = "states/${file.toRelativeString(defaultStatesDirectory)}"
            val targetFile = findOrCreateDocumentFile(customDocumentFile, relativePath)
            if (targetFile != null && targetFile.isFile) {
                appContext.contentResolver.openOutputStream(targetFile.uri)?.use { outputStream ->
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }

    suspend fun syncFromCustomDirectory() = withContext(Dispatchers.IO) {
        val customUri = customSavesDirectoryUri ?: return@withContext
        val customDocumentFile = DocumentFile.fromTreeUri(appContext, customUri) ?: return@withContext

        val contentResolver = appContext.contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
        try {
            contentResolver.takePersistableUriPermission(customUri, takeFlags)
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to take persistable URI permission for: %s", customUri)
            return@withContext
        }

        customDocumentFile.walkDocumentTree().filter { it.isFile }.forEach { documentFile ->
            val relativePath = getRelativePath(customDocumentFile, documentFile)
            if (relativePath.startsWith("saves/")) {
                val targetFile = File(defaultSavesDirectory, relativePath.removePrefix("saves/"))
                targetFile.parentFile?.mkdirs()
                appContext.contentResolver.openInputStream(documentFile.uri)?.use { inputStream ->
                    targetFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } else if (relativePath.startsWith("states/")) {
                val targetFile = File(defaultStatesDirectory, relativePath.removePrefix("states/"))
                targetFile.parentFile?.mkdirs()
                appContext.contentResolver.openInputStream(documentFile.uri)?.use { inputStream ->
                    targetFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }

    private fun findOrCreateDocumentFile(root: DocumentFile, relativePath: String): DocumentFile? {
        var current: DocumentFile? = root
        val parts = relativePath.split(File.separator)
        for (i in 0 until parts.size - 1) {
            current = current?.findFile(parts[i]) ?: current?.createDirectory(parts[i])
            if (current == null || !current.isDirectory) return null
        }
        return current?.findFile(parts.last()) ?: current?.createFile("", parts.last())
    }

    private fun DocumentFile.walkDocumentTree(): Sequence<DocumentFile> = sequence {
        if (isDirectory) {
            val children = listFiles()
            for (child in children) {
                yield(child)
                if (child.isDirectory) {
                    yieldAll(child.walkDocumentTree())
                }
            }
        }
    }

    private fun getRelativePath(root: DocumentFile, file: DocumentFile): String {
        val rootUriPath = root.uri.toString()
        val fileUriPath = file.uri.toString()

        return fileUriPath.removePrefix(rootUriPath).removePrefix("/")
    }
}
