package com.swordfish.lemuroid.app.shared.savesync

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import com.swordfish.lemuroid.app.utils.android.createSyncForegroundInfo
import com.swordfish.lemuroid.lib.injection.AndroidWorkerInjection
import com.swordfish.lemuroid.lib.injection.WorkerKey
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LocalSaveSyncWork(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var directoriesManager: DirectoriesManager

    override suspend fun doWork(): Result {
        AndroidWorkerInjection.inject(this)

        val legacyPreferences = SharedPreferencesHelper.getLegacySharedPreferences(applicationContext)
        val prefKey = applicationContext.getString(com.swordfish.lemuroid.lib.R.string.pref_key_local_save_sync_folder)
        val uriString = legacyPreferences.getString(prefKey, null) ?: return Result.success()

        val harmonyPreferences = SharedPreferencesHelper.getSharedPreferences(applicationContext)
        val exportExtKey = applicationContext.getString(com.swordfish.lemuroid.R.string.pref_key_local_save_sync_export_extension)
        val exportExtValue = harmonyPreferences.getString(exportExtKey, "default") ?: "default"

        if (uriString.isEmpty()) return Result.success()

        displayNotification()

        val destDirUri = Uri.parse(uriString)
        val destDir = DocumentFile.fromTreeUri(applicationContext, destDirUri)
            ?: return Result.failure()

        try {
            withContext(Dispatchers.IO) {
                // Sync Saves
                val savesDir = directoriesManager.getSavesDirectory()
                if (savesDir.exists()) {
                    val savesDest = getOrCreateDirectory(destDir, "saves")
                    if (savesDest != null) {
                        syncFiles(savesDir, savesDest, exportExtValue)
                    }
                }

                // Sync States
                val statesDir = directoriesManager.getStatesDirectory()
                if (statesDir.exists()) {
                    val statesDest = getOrCreateDirectory(destDir, "states")
                    if (statesDest != null) {
                        syncFiles(statesDir, statesDest, exportExtValue)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during local save sync")
            return Result.retry()
        }

        return Result.success()
    }

    private fun getOrCreateDirectory(parent: DocumentFile, name: String): DocumentFile? {
        val existing = parent.findFile(name)
        if (existing != null && existing.isDirectory) {
            return existing
        }
        return parent.createDirectory(name)
    }

    private fun syncFiles(sourceDir: File, destDir: DocumentFile, exportExtValue: String) {
        val files = sourceDir.listFiles() ?: return
        for (file in files) {
            if (file.isFile) {
                var finalName = file.name
                
                if (exportExtValue == "sav" && (finalName.endsWith(".srm") || finalName.endsWith(".dsv"))) {
                    finalName = finalName.substringBeforeLast(".") + ".sav"
                }

                val existing = destDir.findFile(finalName)
                var destFile = existing

                if (existing == null) {
                    destFile = destDir.createFile("application/octet-stream", finalName)
                }

                destFile?.let { dFile ->
                    applicationContext.contentResolver.openOutputStream(dFile.uri)?.use { outStream ->
                        FileInputStream(file).use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                }
            } else if (file.isDirectory) {
                val subDestDir = getOrCreateDirectory(destDir, file.name)
                if (subDestDir != null) {
                    syncFiles(file, subDestDir, exportExtValue)
                }
            }
        }
    }

    private fun displayNotification() {
        val notificationsManager = NotificationsManager(applicationContext)
        val foregroundInfo = createSyncForegroundInfo(
            NotificationsManager.SAVE_SYNC_NOTIFICATION_ID,
            notificationsManager.saveSyncNotification(),
        )
        setForegroundAsync(foregroundInfo)
    }

    companion object {
        val UNIQUE_WORK_ID: String = LocalSaveSyncWork::class.java.simpleName
        val UNIQUE_PERIODIC_WORK_ID: String = LocalSaveSyncWork::class.java.simpleName + "Periodic"

        fun enqueueManualWork(applicationContext: Context) {
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<LocalSaveSyncWork>()
                    .build(),
            )
        }

        fun enqueueAutoWork(applicationContext: Context) {
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK_ID,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<LocalSaveSyncWork>(4, TimeUnit.HOURS)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .build(),
                    )
                    .build(),
            )
        }
    }

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds
        @IntoMap
        @WorkerKey(LocalSaveSyncWork::class)
        abstract fun bindMyWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out ListenableWorker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<LocalSaveSyncWork> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<LocalSaveSyncWork>()
    }
}
