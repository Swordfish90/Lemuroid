package com.swordfish.lemuroid.app.shared.saves

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Exports and imports the user save data (in-game SRAM/battery saves, save states and their
 * previews) to and from a single zip archive picked through the Storage Access Framework.
 *
 * This works completely offline and is independent from the optional cloud "Save Sync" feature,
 * so it is available on every build flavour.
 */
class SavesImportExportManager(private val directoriesManager: DirectoriesManager) {
    private data class Section(val prefix: String, val directory: File)

    private fun sections(): List<Section> =
        listOf(
            Section(SAVES_PREFIX, directoriesManager.getSavesDirectory()),
            Section(STATES_PREFIX, directoriesManager.getStatesDirectory()),
            Section(STATES_PREVIEW_PREFIX, directoriesManager.getStatesPreviewDirectory()),
        )

    /** Number of save-related files currently on disk (used to warn the user when there is nothing to export). */
    suspend fun countLocalFiles(): Int =
        withContext(Dispatchers.IO) {
            sections().sumOf { section ->
                section.directory.walkTopDown().count { it.isFile }
            }
        }

    suspend fun export(
        context: Context,
        destination: Uri,
    ) = withContext(Dispatchers.IO) {
        val outputStream =
            context.contentResolver.openOutputStream(destination)
                ?: throw IllegalStateException("Unable to open output stream for $destination")

        outputStream.use { os ->
            ZipOutputStream(os.buffered()).use { zos ->
                sections().forEach { section ->
                    val root = section.directory
                    root.walkTopDown()
                        .filter { it.isFile }
                        .forEach { file ->
                            val relative = file.relativeTo(root).invariantSeparatorsPath
                            zos.putNextEntry(ZipEntry("${section.prefix}/$relative"))
                            file.inputStream().use { it.copyTo(zos) }
                            zos.closeEntry()
                        }
                }
            }
        }
    }

    suspend fun import(
        context: Context,
        source: Uri,
    ) = withContext(Dispatchers.IO) {
        val roots = sections().associate { it.prefix to it.directory }

        val inputStream =
            context.contentResolver.openInputStream(source)
                ?: throw IllegalStateException("Unable to open input stream for $source")

        inputStream.use { input ->
            ZipInputStream(input.buffered()).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        writeEntry(roots, entry, zis)
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }
    }

    private fun writeEntry(
        roots: Map<String, File>,
        entry: ZipEntry,
        zis: ZipInputStream,
    ) {
        val path = entry.name.replace('\\', '/')
        val prefix = roots.keys.firstOrNull { path == it || path.startsWith("$it/") }
        if (prefix == null) {
            Timber.w("Skipping unrecognized zip entry: ${entry.name}")
            return
        }

        val relative = path.removePrefix("$prefix/")
        if (!isSafeRelativePath(relative)) {
            Timber.w("Skipping unsafe zip entry: ${entry.name}")
            return
        }

        val outFile = File(roots.getValue(prefix), relative)
        outFile.parentFile?.mkdirs()
        outFile.outputStream().use { zis.copyTo(it) }
    }

    /** Guards against Zip Slip: reject absolute paths or any ".." traversal. */
    private fun isSafeRelativePath(relative: String): Boolean {
        if (relative.isBlank() || relative.startsWith("/")) return false
        return relative.split("/").none { it == ".." || it.isBlank() }
    }

    /**
     * Imports a single in-game (SRAM) save file into the saves directory, keeping its original
     * name. Useful when the user just wants to restore one game's save without a full zip backup.
     * Returns the file name that was written.
     */
    suspend fun importSingleSaveFile(
        context: Context,
        source: Uri,
    ): String =
        withContext(Dispatchers.IO) {
            val displayName = queryDisplayName(context, source) ?: "imported.srm"
            val safeName = displayName.substringAfterLast('/').substringAfterLast('\\')
            require(safeName.isNotBlank()) { "Invalid file name for $source" }

            val target = File(directoriesManager.getSavesDirectory(), safeName)
            val input =
                context.contentResolver.openInputStream(source)
                    ?: throw IllegalStateException("Unable to open input stream for $source")
            input.use { inputStream -> target.outputStream().use { inputStream.copyTo(it) } }
            safeName
        }

    private fun queryDisplayName(
        context: Context,
        uri: Uri,
    ): String? {
        return context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
    }

    companion object {
        private const val SAVES_PREFIX = "saves"
        private const val STATES_PREFIX = "states"
        private const val STATES_PREVIEW_PREFIX = "state-previews"

        fun defaultExportFileName(): String = "lemuroid-saves.zip"
    }
}
