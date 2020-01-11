package com.swordfish.lemuroid.lib.core.assetsmanager

import com.swordfish.lemuroid.lib.core.CoreManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import timber.log.Timber
import java.io.File

class PPSSPPAssetsManager: CoreManager.AssetsManager {

    override fun clearAssets(directoriesManager: DirectoriesManager) = Completable.fromAction {
        getAssetsDirectory(directoriesManager).deleteRecursively()
    }

    override fun retrieveAssets(
        coreManagerApi: CoreManager.CoreManagerApi,
        directoriesManager: DirectoriesManager
    ): Completable {
        return coreManagerApi.downloadZip(PPSSPP_ASSETS_URL).doOnSuccess { response ->
            val coreAssetsDirectory = getAssetsDirectory(directoriesManager)
            coreAssetsDirectory.mkdirs()

            response.body()?.use { zipInputStream ->
                while (true) {
                    val entry = zipInputStream.nextEntry ?: break
                    Timber.d("Reading file: ${entry.name}")
                    if (entry.name.startsWith(BASE_ARCHIVE_DIRECTORY)) {
                        Timber.d("Writing file: ${entry.name}")
                        val destFile = File(coreAssetsDirectory, entry.name.replace(BASE_ARCHIVE_DIRECTORY, ""))
                        if (entry.isDirectory) {
                            destFile.mkdirs()
                        } else {
                            zipInputStream.copyTo(destFile.outputStream())
                        }
                    }
                }
            }
        }
        .ignoreElement()
    }

    private fun getAssetsDirectory(directoriesManager: DirectoriesManager) =
            File(directoriesManager.getSystemDirectory(), PPSSPP_ASSETS_FOLDER_NAME)

    companion object {
        const val PPSSPP_ASSETS_URL = "https://github.com/hrydgard/ppsspp/archive/master.zip"
        const val PPSSPP_ASSETS_FOLDER_NAME = "PPSSPP"
        const val BASE_ARCHIVE_DIRECTORY = "ppsspp-master/assets"
    }
}
