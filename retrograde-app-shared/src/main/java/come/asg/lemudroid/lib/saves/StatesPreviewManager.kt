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
            val file = getPreviewFile(getSlotScreenshotName(game, index), coreID.coreName)
            val modernFile = getPreviewFile(getHashedSlotScreenshotName(game, coreID, index), coreID.coreName)
            val bitmap = BitmapFactory.decodeFile((if (file.exists()) file else modernFile).absolutePath)
            bitmap?.let { ThumbnailUtils.extractThumbnail(it, size, size) }
        }

    suspend fun setPreviewForSlot(
        game: Game,
        bitmap: Bitmap,
        coreID: CoreID,
        index: Int,
    ) = withContext(Dispatchers.IO) {
        val screenshotName = getSlotScreenshotName(game, index)
        val file = getPreviewFile(screenshotName, coreID.coreName)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
    }

    private fun getPreviewFile(
        fileName: String,
        coreName: String,
    ): File {
        val statesDirectories = File(directoriesManager.getStatesPreviewDirectory(), coreName.safeBasename())
        statesDirectories.mkdirs()
        return File(statesDirectories, fileName)
    }

    private fun getSlotScreenshotName(
        game: Game,
        index: Int,
    ) = "${game.fileName.safeBasename()}.slot${index + 1}.jpg"

    private fun getHashedSlotScreenshotName(
        game: Game,
        coreID: CoreID,
        index: Int,
    ) = "${game.fileName.safeBasename()}-${stableGameIdentity(
        game.fileUri,
        game.systemId,
        coreID.coreName,
    )}.slot${index + 1}.jpg"

    companion object {
        val PREVIEW_SIZE_DP = 96f
    }
}
