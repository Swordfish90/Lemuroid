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
        performSync(applicationContext)
        displayNotification()
        return Result.success()
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

        suspend fun performSync(applicationContext: Context) {
            val legacyPreferences = SharedPreferencesHelper.getLegacySharedPreferences(applicationContext)
            val prefKey = applicationContext.getString(com.swordfish.lemuroid.lib.R.string.pref_key_local_save_sync_folder)
            val uriString = legacyPreferences.getString(prefKey, null) ?: return

            val harmonyPreferences = SharedPreferencesHelper.getSharedPreferences(applicationContext)
            val exportExtKey = applicationContext.getString(com.swordfish.lemuroid.R.string.pref_key_local_save_sync_export_extension)
            val exportExtValue = harmonyPreferences.getString(exportExtKey, "default") ?: "default"

            if (uriString.isEmpty()) return

            val destDirUri = Uri.parse(uriString)
            val destDir = DocumentFile.fromTreeUri(applicationContext, destDirUri) ?: return

            val directoriesManager = DirectoriesManager(applicationContext)
            try {
                withContext(Dispatchers.IO) {
                    val savesDir = directoriesManager.getSavesDirectory()
                    if (savesDir.exists()) {
                        val savesDest = getOrCreateDirectory(destDir, "saves")
                        if (savesDest != null) {
                            syncFiles(savesDir, savesDest, exportExtValue, applicationContext)
                        }
                    }

                    val statesDir = directoriesManager.getStatesDirectory()
                    if (statesDir.exists()) {
                        val statesDest = getOrCreateDirectory(destDir, "states")
                        if (statesDest != null) {
                            syncFiles(statesDir, statesDest, exportExtValue, applicationContext)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during local save sync")
            }
        }

        private fun getOrCreateDirectory(parent: DocumentFile, name: String): DocumentFile? {
            val existing = parent.findFile(name)
            if (existing != null && existing.isDirectory) {
                return existing
            }
            return parent.createDirectory(name)
        }

        private fun syncFiles(sourceDir: File, destDir: DocumentFile, exportExtValue: String, applicationContext: Context) {
            val syncStateFile = File(sourceDir, ".lemuroid_sync_state")
            val syncMap = mutableMapOf<String, Long>()
            if (syncStateFile.exists()) {
                try {
                    val lines = syncStateFile.readLines()
                    for (line in lines) {
                        val parts = line.split("|")
                        if (parts.size == 2) {
                            syncMap[parts[0]] = parts[1].toLong()
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to read sync state file")
                }
            }

            val intFiles = sourceDir.listFiles()?.filter { !it.name.startsWith(".lemuroid_sync_state") } ?: emptyList()
            val extFiles = destDir.listFiles()

            // 1. Process Internal Files
            for (intFile in intFiles) {
                if (intFile.isDirectory) {
                    val subDestDir = getOrCreateDirectory(destDir, intFile.name)
                    if (subDestDir != null) {
                        syncFiles(intFile, subDestDir, exportExtValue, applicationContext)
                    }
                    continue
                }

                val intName = intFile.name
                var extName = intName
                if (exportExtValue == "sav" && (extName.endsWith(".srm") || extName.endsWith(".dsv"))) {
                    extName = extName.substringBeforeLast(".") + ".sav"
                }

                val extFile = destDir.findFile(extName)
                
                val lastIntSync = syncMap["INT_$intName"] ?: 0L
                val lastExtSync = syncMap["EXT_$extName"] ?: 0L

                val intChanged = Math.abs(intFile.lastModified() - lastIntSync) > 2000L
                val extChanged = extFile != null && Math.abs(extFile.lastModified() - lastExtSync) > 2000L

                val shouldSyncIntToExt = if (extFile == null) {
                    true // External doesn't exist, we must push it
                } else if (intChanged && !extChanged) {
                    true // internal only changed
                } else if (intChanged && extChanged) {
                    intFile.lastModified() > extFile.lastModified() // Conflict! newer wins
                } else {
                    false // neither changed, or only external changed (handled in phase 2)
                }

                if (shouldSyncIntToExt) {
                    val destFile = extFile ?: destDir.createFile("application/octet-stream", extName)
                    destFile?.let { dFile ->
                        applicationContext.contentResolver.openOutputStream(dFile.uri)?.use { outStream ->
                            FileInputStream(intFile).use { inStream ->
                                inStream.copyTo(outStream)
                            }
                        }
                        syncMap["INT_$intName"] = intFile.lastModified()
                        syncMap["EXT_$extName"] = dFile.lastModified()
                    }
                }
            }

            // 2. Process remaining External Files that were changed/dropped
            for (extFile in extFiles) {
                if (extFile.isDirectory) continue
                val extName = extFile.name ?: continue
                
                var intName = extName
                if (exportExtValue == "sav" && extName.endsWith(".sav")) {
                    val baseName = extName.substringBeforeLast(".")
                    intName = if (File(sourceDir, "$baseName.dsv").exists()) {
                        "$baseName.dsv"
                    } else {
                        "$baseName.srm"
                    }
                }

                val intFile = File(sourceDir, intName)
                
                val lastIntSync = syncMap["INT_$intName"] ?: 0L
                val lastExtSync = syncMap["EXT_$extName"] ?: 0L

                val intChanged = intFile.exists() && Math.abs(intFile.lastModified() - lastIntSync) > 2000L
                val extChanged = Math.abs(extFile.lastModified() - lastExtSync) > 2000L

                val shouldSyncExtToInt = if (!intFile.exists()) {
                    true // Internal missing, pull it!
                } else if (extChanged && !intChanged) {
                    true // External only changed (like user dropping an old file)
                } else if (extChanged && intChanged) {
                    extFile.lastModified() > intFile.lastModified() // conflict
                } else {
                    false
                }

                if (shouldSyncExtToInt) {
                    applicationContext.contentResolver.openInputStream(extFile.uri)?.use { inStream ->
                        java.io.FileOutputStream(intFile).use { outStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    syncMap["INT_$intName"] = intFile.lastModified()
                    syncMap["EXT_$extName"] = extFile.lastModified()
                }
            }

            // Save state
            try {
                syncStateFile.bufferedWriter().use { out ->
                    for ((k, v) in syncMap) {
                        out.write("$k|$v\n")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to write sync state file")
            }
        }

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
