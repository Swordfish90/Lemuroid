package come.asg.lemudroid.lib.saves

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import come.asg.lemudroid.common.kotlin.safeBasename
import come.asg.lemudroid.common.kotlin.stableGameIdentity
import come.asg.lemudroid.lib.library.CoreID
import come.asg.lemudroid.lib.library.db.entity.Game
import come.asg.lemudroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class StatesPreviewManager(private val directoriesManager: DirectoriesManager) {
    suspend fun getPreviewForSlot(
        game: Game,
        coreID: CoreID,
        index: Int,
        size: Int,
    ): Bitmap? =
        withContext(Dispatchers.IO) {
            migrateLegacyPreview(game, coreID, index)
            val bitmap = BitmapFactory.decodeFile(previewFile(game, coreID, index).absolutePath)
            bitmap?.let { ThumbnailUtils.extractThumbnail(it, size, size) }
        }

    suspend fun setPreviewForSlot(
        game: Game,
        bitmap: Bitmap,
        coreID: CoreID,
        index: Int,
    ) = withContext(Dispatchers.IO) {
        migrateLegacyPreview(game, coreID, index)
        val file = previewFile(game, coreID, index)
        file.parentFile?.mkdirs()
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
    }

    private fun migrateLegacyPreview(game: Game, coreID: CoreID, index: Int) {
        val legacyDirectory = File(directoriesManager.getStatesPreviewDirectory(), coreID.coreName.safeBasename()).apply { mkdirs() }
        val baseName = game.fileName.safeBasename()
        val hashedBaseName = "${baseName}-${stableGameIdentity(game.fileUri, game.systemId, coreID.coreName)}"
        SaveStoragePaths.migrateNewestIfMissing(
            listOf(
                File(legacyDirectory, "$baseName.slot${index + 1}.jpg"),
                File(legacyDirectory, "$hashedBaseName.slot${index + 1}.jpg"),
            ),
            previewFile(game, coreID, index),
        )
    }

    private fun previewDirectory(game: Game, coreID: CoreID): File =
        SaveStoragePaths.gameDirectory(
            File(directoriesManager.getStatesPreviewDirectory(), coreID.coreName.safeBasename()).apply { mkdirs() },
            game,
            "state:${coreID.coreName}",
        )

    private fun previewFile(game: Game, coreID: CoreID, index: Int): File =
        File(previewDirectory(game, coreID), "slot${index + 1}.jpg")

    companion object {
        val PREVIEW_SIZE_DP = 96f
    }
}
