package come.asg.lemudroid.lib.core.assetsmanager

import android.content.SharedPreferences
import android.net.Uri
import come.asg.lemudroid.common.kotlin.extractEntriesToDirectory
import come.asg.lemudroid.lib.core.CoreUpdater
import come.asg.lemudroid.lib.library.CoreID
import come.asg.lemudroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.util.zip.ZipInputStream

class PPSSPPAssetsManager : CoreID.AssetsManager {
    override suspend fun clearAssets(directoriesManager: DirectoriesManager) {
        getAssetsDirectory(directoriesManager).deleteRecursively()
    }

    override suspend fun retrieveAssetsIfNeeded(
        coreUpdaterApi: CoreUpdater.CoreManagerApi,
        directoriesManager: DirectoriesManager,
        sharedPreferences: SharedPreferences,
    ) {
        if (!updatedRequested(directoriesManager, sharedPreferences)) {
            return
        }

        try {
            val response = coreUpdaterApi.downloadZip(PPSSPP_ASSETS_URL.toString())
            handleSuccess(directoriesManager, response, sharedPreferences)
        } catch (e: Throwable) {
            Timber.e(e, "Unable to update PPSSPP assets; keeping the existing installation")
        }
    }

    private suspend fun handleSuccess(
        directoriesManager: DirectoriesManager,
        response: Response<ZipInputStream>,
        sharedPreferences: SharedPreferences,
    ) {
        val coreAssetsDirectory = getAssetsDirectory(directoriesManager)
        val temporaryDirectory = File(coreAssetsDirectory.parentFile, "${coreAssetsDirectory.name}.tmp")
        temporaryDirectory.deleteRecursively()
        temporaryDirectory.mkdirs()

        try {
            response.body()?.use { zipInputStream ->
                zipInputStream.extractEntriesToDirectory(temporaryDirectory)
            } ?: throw IllegalStateException("Empty PPSSPP assets response")
            coreAssetsDirectory.deleteRecursively()
            if (!temporaryDirectory.renameTo(coreAssetsDirectory)) {
                throw IllegalStateException("Unable to install PPSSPP assets")
            }
        } catch (e: Throwable) {
            temporaryDirectory.deleteRecursively()
            throw e
        }

        sharedPreferences.edit()
            .putString(PPSSPP_ASSETS_VERSION_KEY, PPSSPP_ASSETS_VERSION)
            .commit()
    }

    private suspend fun updatedRequested(
        directoriesManager: DirectoriesManager,
        sharedPreferences: SharedPreferences,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val directoryExists = getAssetsDirectory(directoriesManager).exists()

            val currentVersion = sharedPreferences.getString(PPSSPP_ASSETS_VERSION_KEY, "none")
            val hasCurrentVersion = currentVersion == PPSSPP_ASSETS_VERSION

            !directoryExists || !hasCurrentVersion
        }

    private suspend fun getAssetsDirectory(directoriesManager: DirectoriesManager): File {
        return withContext(Dispatchers.IO) {
            File(directoriesManager.getSystemDirectory(), PPSSPP_ASSETS_FOLDER_NAME)
        }
    }

    companion object {
        const val PPSSPP_ASSETS_VERSION = "1.15"

        val PPSSPP_ASSETS_URL: Uri =
            Uri.parse("https://github.com/Swordfish90/OMINOYCores/")
                .buildUpon()
                .appendEncodedPath("raw/$PPSSPP_ASSETS_VERSION/assets/ppsspp.zip")
                .build()

        const val PPSSPP_ASSETS_VERSION_KEY = "ppsspp_assets_version_key"

        const val PPSSPP_ASSETS_FOLDER_NAME = "PPSSPP"
    }
}
