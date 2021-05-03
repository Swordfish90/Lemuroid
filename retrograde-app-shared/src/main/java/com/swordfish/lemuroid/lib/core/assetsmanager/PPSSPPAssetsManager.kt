package com.swordfish.lemuroid.lib.core.assetsmanager

import android.content.SharedPreferences
import android.net.Uri
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.util.zip.ZipInputStream

class PPSSPPAssetsManager : CoreID.AssetsManager {

    override fun clearAssets(directoriesManager: DirectoriesManager) = Completable.fromAction {
        getAssetsDirectory(directoriesManager).deleteRecursively()
    }

    override fun retrieveAssetsIfNeeded(
        coreUpdaterApi: CoreUpdater.CoreManagerApi,
        directoriesManager: DirectoriesManager,
        sharedPreferences: SharedPreferences
    ): Completable {

        return updatedRequested(directoriesManager, sharedPreferences)
            .filter { it }
            .flatMapCompletable {
                coreUpdaterApi.downloadZip(PPSSPP_ASSETS_URL.toString())
                    .doOnSuccess { handleSuccess(directoriesManager, it, sharedPreferences) }
                    .doOnError { getAssetsDirectory(directoriesManager).deleteRecursively() }
                    .ignoreElement()
            }
    }

    private fun handleSuccess(
        directoriesManager: DirectoriesManager,
        response: Response<ZipInputStream>,
        sharedPreferences: SharedPreferences
    ) {
        val coreAssetsDirectory = getAssetsDirectory(directoriesManager)
        coreAssetsDirectory.deleteRecursively()
        coreAssetsDirectory.mkdirs()

        response.body()?.use { zipInputStream ->
            while (true) {
                val entry = zipInputStream.nextEntry ?: break
                Timber.d("Writing file: ${entry.name}")
                val destFile = File(
                    coreAssetsDirectory,
                    entry.name
                )
                if (entry.isDirectory) {
                    destFile.mkdirs()
                } else {
                    zipInputStream.copyTo(destFile.outputStream())
                }
            }
        }

        sharedPreferences.edit()
            .putString(PPSSPP_ASSETS_VERSION_KEY, PPSSPP_ASSETS_VERSION)
            .commit()
    }

    private fun updatedRequested(
        directoriesManager: DirectoriesManager,
        sharedPreferences: SharedPreferences
    ): Single<Boolean> {
        val directoryExists = Single.fromCallable {
            getAssetsDirectory(directoriesManager).exists()
        }

        val hasCurrentVersion = Single
            .fromCallable { sharedPreferences.getString(PPSSPP_ASSETS_VERSION_KEY, "none") }
            .map { it == PPSSPP_ASSETS_VERSION }

        return Singles.zip(directoryExists, hasCurrentVersion) { a, b -> !a || !b }
    }

    private fun getAssetsDirectory(directoriesManager: DirectoriesManager) =
        File(directoriesManager.getSystemDirectory(), PPSSPP_ASSETS_FOLDER_NAME)

    companion object {
        const val PPSSPP_ASSETS_VERSION = "1.11"

        val PPSSPP_ASSETS_URL: Uri = Uri.parse("https://github.com/Swordfish90/LemuroidCores/")
            .buildUpon()
            .appendEncodedPath("raw/$PPSSPP_ASSETS_VERSION/assets/ppsspp.zip")
            .build()

        const val PPSSPP_ASSETS_VERSION_KEY = "ppsspp_assets_version_key"

        const val PPSSPP_ASSETS_FOLDER_NAME = "PPSSPP"
    }
}
