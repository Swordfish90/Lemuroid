package come.asg.ominoy.lib.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import come.asg.ominoy.lib.R
import come.asg.ominoy.lib.preferences.SharedPreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Mirrors emulator saves between the app's path-based working storage and a user-selected SAF tree.
 * Libretro cores still use the local path; SAF is only accessed through ContentResolver streams.
 */
internal class SaveFolderMirror(
    private val context: Context,
    private val directoriesManager: DirectoriesManager,
) {
    suspend fun importToLocal() = withContext(Dispatchers.IO) {
        val root = selectedRoot() ?: return@withContext
        listOf("saves" to directoriesManager.getSavesDirectory(), "states" to directoriesManager.getStatesDirectory(), "achievements" to directoriesManager.getAchievementsDirectory())
            .forEach { (name, localDir) ->
                val remoteDir = root.findFile(name) ?: return@forEach
                copyTreeToLocal(remoteDir, localDir)
            }
    }

    suspend fun exportToSaf() = withContext(Dispatchers.IO) {
        val root = selectedRoot() ?: return@withContext
        listOf("saves" to directoriesManager.getSavesDirectory(), "states" to directoriesManager.getStatesDirectory(), "achievements" to directoriesManager.getAchievementsDirectory())
            .forEach { (name, localDir) ->
                val remoteDir = root.findFile(name) ?: root.createDirectory(name)
                    ?: throw IOException("Could not create $name in selected save folder")
                copyTreeToSaf(localDir, remoteDir, overwriteExisting = true)
            }
    }

    suspend fun copyExistingToSafWithoutOverwriting() = withContext(Dispatchers.IO) {
        val root = selectedRoot() ?: return@withContext
        listOf("saves" to directoriesManager.getSavesDirectory(), "states" to directoriesManager.getStatesDirectory(), "achievements" to directoriesManager.getAchievementsDirectory())
            .forEach { (name, localDir) ->
                val remoteDir = root.findFile(name) ?: root.createDirectory(name)
                    ?: throw IOException("Could not create $name in selected save folder")
                copyTreeToSaf(localDir, remoteDir, overwriteExisting = false)
            }
    }

    suspend fun exportAchievementsToDataFolder() = withContext(Dispatchers.IO) {
        val key = context.getString(R.string.pref_key_app_data_folder)
        val uriText = SharedPreferencesHelper.getLegacySharedPreferences(context).getString(key, null) ?: return@withContext
        val root = runCatching { DocumentFile.fromTreeUri(context, Uri.parse(uriText)) }.getOrNull()
            ?.takeIf { it.exists() && it.isDirectory && it.canWrite() } ?: return@withContext
        val remoteDir = root.findFile("achievements") ?: root.createDirectory("achievements")
            ?: throw IOException("Could not create achievements in selected data folder")
        val localAchievements = directoriesManager.getAchievementsDirectory()
        val selectedDataDirectory = directoriesManager.getSelectedDataDirectory()
        val sharesLocalPath = selectedDataDirectory != null && runCatching {
            localAchievements.canonicalFile == File(selectedDataDirectory, "achievements").canonicalFile
        }.getOrDefault(false)
        if (!sharesLocalPath) {
            // Achievements are mirrored as JSON only. Badge images remain app-private.
            copyJsonTreeToSaf(localAchievements, remoteDir)
        }
    }

    private fun selectedRoot(): DocumentFile? {
        val key = context.getString(R.string.pref_key_save_folder)
        val uriText = SharedPreferencesHelper.getLegacySharedPreferences(context).getString(key, null) ?: return null
        return runCatching { DocumentFile.fromTreeUri(context, Uri.parse(uriText)) }
            .onFailure { Timber.w(it, "Selected save folder is unavailable") }
            .getOrNull()
            ?.takeIf { it.exists() && it.isDirectory && it.canRead() && it.canWrite() }
    }

    private fun copyTreeToLocal(source: DocumentFile, destination: File) {
        if (!destination.exists() && !destination.mkdirs()) throw IOException("Could not create $destination")
        source.listFiles().forEach { child ->
            val target = File(destination, safeName(child.name))
            if (child.isDirectory) {
                copyTreeToLocal(child, target)
            } else if (child.isFile) {
                val sourceModified = child.lastModified()
                val input = context.contentResolver.openInputStream(child.uri) ?: throw IOException("Cannot read ${child.name}")
                val temporary = File(target.parentFile, ".${target.name}.saf-copy")
                try {
                    input.use { inStream -> temporary.outputStream().use { inStream.copyTo(it) } }
                    if (!temporary.renameTo(target)) {
                        temporary.copyTo(target, overwrite = true)
                        temporary.delete()
                    }
                    if (sourceModified > 0) target.setLastModified(sourceModified)
                } finally {
                    temporary.delete()
                }
            }
        }
    }

    private fun copyTreeToSaf(
        source: File,
        destination: DocumentFile,
        overwriteExisting: Boolean,
        forceOverwrite: Boolean = false,
    ) {
        if (!source.exists()) return
        source.listFiles()?.forEach { child ->
            val name = safeName(child.name)
            if (child.isDirectory) {
                val target = destination.findFile(name)?.takeIf { it.isDirectory } ?: destination.createDirectory(name)
                    ?: throw IOException("Could not create $name")
                copyTreeToSaf(child, target, overwriteExisting, forceOverwrite)
            } else if (child.isFile && child.length() > 0) {
                val existing = destination.findFile(name)
                if (existing != null && !overwriteExisting) return@forEach
                if (!forceOverwrite && existing != null && existing.lastModified() >= child.lastModified() && existing.length() == child.length()) return@forEach
                existing?.delete()
                val target = destination.createFile("application/octet-stream", name)
                    ?: throw IOException("Could not create $name")
                val input = child.inputStream()
                val output = context.contentResolver.openOutputStream(target.uri, "w")
                    ?: throw IOException("Cannot write $name")
                val copiedBytes = input.use { inStream -> output.use { inStream.copyTo(it) } }
                if (copiedBytes != child.length()) throw IOException("Incomplete copy of $name")
            }
        }
    }

    private fun copyJsonTreeToSaf(source: File, destination: DocumentFile) {
        if (!source.exists()) return
        source.listFiles()?.forEach { child ->
            if (child.isDirectory) {
                val childName = safeName(child.name)
                val target = destination.findFile(childName)?.takeIf { it.isDirectory }
                    ?: destination.createDirectory(childName)
                    ?: throw IOException("Could not create $childName")
                copyJsonTreeToSaf(child, target)
            } else if (child.isFile && child.extension == "json") {
                val childName = safeName(child.name)
                destination.findFile(childName)?.delete()
                val target = destination.createFile("application/json", childName)
                    ?: throw IOException("Could not create $childName")
                val output = context.contentResolver.openOutputStream(target.uri, "w")
                    ?: throw IOException("Cannot write $childName")
                child.inputStream().use { input -> output.use { input.copyTo(it) } }
            }
        }
    }

    private fun safeName(name: String?): String {
        val value = name?.takeIf { it.isNotBlank() && it != "." && it != ".." && '/' !in it && '\\' !in it }
            ?: throw IOException("Invalid document name")
        return value
    }
}
