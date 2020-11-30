package com.swordfish.lemuroid.lib.saves

import android.graphics.Bitmap
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream

class StatesPreviewManager(private val directoriesManager: DirectoriesManager) {

    fun getPreviewsForSlots(
        game: Game,
        coreID: CoreID
    ): Single<List<File?>> = Single.fromCallable {
        (0 until StatesManager.MAX_STATES)
            .map { getPreviewFile(getSlotScreenshotName(game, it), coreID.coreName) }
            .map { if (it.exists()) it else null }
            .toList()
    }

    fun setPreviewForSlot(game: Game, bitmap: Bitmap, coreID: CoreID, index: Int) = Completable.fromAction {
        val screenshotName = getSlotScreenshotName(game, index)
        val file = getPreviewFile(screenshotName, coreID.coreName)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
    }

    private fun getPreviewFile(fileName: String, coreName: String): File {
        val statesDirectories = File(directoriesManager.getStatesPreviewDirectory(), coreName)
        statesDirectories.mkdirs()
        return File(statesDirectories, fileName)
    }

    private fun getSlotScreenshotName(game: Game, index: Int) = "${game.fileName}.slot${index + 1}.jpg"

    companion object {
        val PREVIEW_SIZE_DP = 96f
    }
}