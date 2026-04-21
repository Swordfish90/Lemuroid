package com.swordfish.lemuroid.app.shared.catalog

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.swordfish.lemuroid.lib.injection.AndroidWorkerInjection
import com.swordfish.lemuroid.lib.injection.WorkerKey
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class CatalogSyncWork(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    override suspend fun doWork(): Result {
        AndroidWorkerInjection.inject(this)

        return withContext(Dispatchers.IO) {
            runCatching { sync() }
                .onFailure { Timber.e(it, "CatalogSyncWork failed") }
            Result.success()
        }
    }

    private suspend fun sync() {
        // Clean up legacy entries that used raw paths instead of file:// URIs
        retrogradeDb.gameDao().deleteLegacyCatalogGames()

        val jsonString = applicationContext.assets.open("catalog/meta.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(jsonString)
        val platforms = root.getJSONObject("platforms")

        for (platform in platforms.keys()) {
            val games = platforms.getJSONArray(platform)
            for (i in 0 until games.length()) {
                val entry = games.getJSONObject(i)
                val title = entry.getString("title")
                val file = entry.getString("file")       // e.g. "3ds/snake.3dsx"
                val icon = entry.getString("icon")       // e.g. "3ds/snake.png"
                val description = entry.getString("description")
                val link = entry.optString("link").takeIf { it.isNotEmpty() }

                try {
                    copyAssetIfNeeded("catalog/$file", file)
                    copyAssetIfNeeded("catalog/$icon", icon)
                    upsertGame(platform, file, icon, title, description, link)
                } catch (e: Exception) {
                    Timber.e(e, "CatalogSyncWork: failed to sync game '$title'")
                }
            }
        }
    }

    private fun copyAssetIfNeeded(assetPath: String, relativePath: String) {
        val destFile = File(applicationContext.filesDir, "catalog/$relativePath")
        if (destFile.exists() && destFile.length() > 0) return

        destFile.parentFile?.mkdirs()
        applicationContext.assets.open(assetPath).use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    private suspend fun upsertGame(
        platform: String,
        file: String,
        icon: String,
        title: String,
        description: String,
        catalogLink: String?,
    ) {
        val fileUri = "file://${applicationContext.filesDir}/catalog/$file"
        val coverFrontUrl = "file://${applicationContext.filesDir}/catalog/$icon"
        val fileName = file.substringAfterLast('/')
        val now = System.currentTimeMillis()

        val dao = retrogradeDb.gameDao()
        val existing = dao.selectByFileUri(fileUri)

        if (existing != null) {
            dao.update(
                existing.copy(
                    lastIndexedAt = now,
                    isCatalogGame = true,
                    description = description,
                    catalogLink = catalogLink,
                    coverFrontUrl = coverFrontUrl,
                )
            )
        } else {
            dao.insert(
                Game(
                    fileName = fileName,
                    fileUri = fileUri,
                    title = title,
                    systemId = platform,
                    developer = null,
                    coverFrontUrl = coverFrontUrl,
                    lastIndexedAt = now,
                    isCatalogGame = true,
                    description = description,
                    catalogLink = catalogLink,
                )
            )
        }
    }

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds
        @IntoMap
        @WorkerKey(CatalogSyncWork::class)
        abstract fun bindWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out ListenableWorker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<CatalogSyncWork> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<CatalogSyncWork>()
    }

    companion object {
        private const val WORK_NAME = "catalog_sync"

        fun schedule(context: Context) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequestBuilder<CatalogSyncWork>().build(),
                )
        }
    }
}
