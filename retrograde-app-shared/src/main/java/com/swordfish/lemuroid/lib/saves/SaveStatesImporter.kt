package com.swordfish.lemuroid.lib.saves

import android.content.Context
import android.net.Uri
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipFile

class SaveStatesImporter(private val directoriesManager: DirectoriesManager) {

    suspend fun importFromUri(
        context: Context,
        game: Game,
        coreID: CoreID,
        uri: Uri,
    ): Result<Int> = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "import_saves_tmp.zip")
        runCatching {
            context.contentResolver.openInputStream(uri)!!.use { input ->
                tempFile.outputStream().use { input.copyTo(it) }
            }

            val statesDir = directoriesManager.getStatesDirectory()
            val savesDir = directoriesManager.getSavesDirectory()
            val baseName = game.fileName.substringBeforeLast(".")

            val zipEntryToFile = mapOf(
                "slot1.state" to File(statesDir, "${coreID.coreName}/${game.fileName}.slot1"),
                "slot1.state.metadata" to File(statesDir, "${coreID.coreName}/${game.fileName}.slot1.metadata"),
                "slot2.state" to File(statesDir, "${coreID.coreName}/${game.fileName}.slot2"),
                "slot2.state.metadata" to File(statesDir, "${coreID.coreName}/${game.fileName}.slot2.metadata"),
                "slot3.state" to File(statesDir, "${coreID.coreName}/${game.fileName}.slot3"),
                "slot3.state.metadata" to File(statesDir, "${coreID.coreName}/${game.fileName}.slot3.metadata"),
                "slot4.state" to File(statesDir, "${coreID.coreName}/${game.fileName}.slot4"),
                "slot4.state.metadata" to File(statesDir, "${coreID.coreName}/${game.fileName}.slot4.metadata"),
                "auto.state" to File(statesDir, "${coreID.coreName}/${game.fileName}.state"),
                "auto.state.metadata" to File(statesDir, "${coreID.coreName}/${game.fileName}.state.metadata"),
                "game.srm" to File(savesDir, "$baseName.srm"),
            )

            ZipFile(tempFile).use { zip ->
                val manifestEntry = zip.getEntry("manifest.json")
                    ?: error("manifest.json missing from archive")
                val manifest = Json.decodeFromString<SaveManifest>(
                    zip.getInputStream(manifestEntry).bufferedReader().readText(),
                )
                if (manifest.coreName != coreID.coreName) {
                    error("Core mismatch: archive is for '${manifest.coreName}', current core is '${coreID.coreName}'")
                }

                var count = 0
                for ((entryName, destFile) in zipEntryToFile) {
                    val entry = zip.getEntry(entryName) ?: continue
                    destFile.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { it.copyTo(destFile.outputStream()) }
                    count++
                }
                count
            }
        }.also {
            tempFile.delete()
        }
    }
}
