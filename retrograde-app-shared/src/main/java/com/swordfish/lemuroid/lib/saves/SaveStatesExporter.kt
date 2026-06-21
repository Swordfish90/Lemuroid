package com.swordfish.lemuroid.lib.saves

import android.content.Context
import android.net.Uri
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SaveStatesExporter(private val directoriesManager: DirectoriesManager) {

    suspend fun exportToUri(
        context: Context,
        game: Game,
        coreID: CoreID,
        uri: Uri,
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val statesDir = directoriesManager.getStatesDirectory()
            val savesDir = directoriesManager.getSavesDirectory()
            val baseName = game.fileName.substringBeforeLast(".")

            val filesToExport =
                listOf(
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

            val manifest = SaveManifest(
                fileName = game.fileName,
                systemId = game.systemId,
                coreName = coreID.coreName,
            )

            var count = 0
            context.contentResolver.openOutputStream(uri)!!.use { outputStream ->
                ZipOutputStream(outputStream).use { zip ->
                    zip.putNextEntry(ZipEntry("manifest.json"))
                    zip.write(Json.encodeToString(manifest).toByteArray())
                    zip.closeEntry()

                    for ((zipName, file) in filesToExport) {
                        if (!file.exists()) continue
                        zip.putNextEntry(ZipEntry(zipName))
                        file.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                        count++
                    }
                }
            }
            count
        }
    }
}
