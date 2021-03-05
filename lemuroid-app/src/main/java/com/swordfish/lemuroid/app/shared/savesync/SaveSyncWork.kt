package com.swordfish.lemuroid.app.shared.savesync

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.swordfish.lemuroid.app.mobile.feature.settings.RxSettingsManager
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import com.swordfish.lemuroid.lib.injection.AndroidWorkerInjection
import com.swordfish.lemuroid.lib.injection.WorkerKey
import com.swordfish.lemuroid.lib.library.findByName
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SaveSyncWork(context: Context, workerParams: WorkerParameters) :
    RxWorker(context, workerParams) {

    @Inject lateinit var saveSyncManager: SaveSyncManager
    @Inject lateinit var settingsManager: RxSettingsManager

    override fun createWork(): Single<Result> {
        AndroidWorkerInjection.inject(this)

        if (!saveSyncManager.isSupported() || !saveSyncManager.isConfigured())
            return Single.just(Result.success())

        return settingsManager.syncSaves
            .filter { it }
            .flatMapCompletable {
                shouldPerformSync()
                    .filter { it }
                    .doOnSuccess { displayNotification() }
                    .flatMapSingle { settingsManager.syncStatesCores }
                    .flatMapCompletable { coreNames ->
                        val coresToSync = coreNames.mapNotNull { findByName(it) }.toSet()
                        saveSyncManager.sync(coresToSync)
                    }
            }
            .subscribeOn(Schedulers.io())
            .doOnError { e -> Timber.e(e, "Error in saves sync") }
            .onErrorComplete()
            .andThen(Single.just(Result.success()))
    }

    private fun shouldPerformSync(): Single<Boolean> {
        val isAutoSync = inputData.getBoolean(IS_AUTO, false)
        val isManualSync = !isAutoSync
        return settingsManager.autoSaveSync
            .map { it && isAutoSync || isManualSync }
    }

    private fun displayNotification() {
        val notificationsManager = NotificationsManager(applicationContext)

        val foregroundInfo = ForegroundInfo(
            NotificationsManager.SAVE_SYNC_NOTIFICATION_ID,
            notificationsManager.saveSyncNotification()
        )
        setForegroundAsync(foregroundInfo)
    }

    companion object {
        val UNIQUE_WORK_ID: String = SaveSyncWork::class.java.simpleName
        val UNIQUE_PERIODIC_WORK_ID: String = SaveSyncWork::class.java.simpleName + "Periodic"
        private const val IS_AUTO = "IS_AUTO"

        fun enqueueManualWork(applicationContext: Context) {
            val inputData: Data = workDataOf(IS_AUTO to false)

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SaveSyncWork>()
                    .setInputData(inputData)
                    .build()
            )
        }

        fun enqueueAutoWork(applicationContext: Context, delayMinutes: Long = 0) {
            val inputData: Data = workDataOf(IS_AUTO to true)

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK_ID,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<SaveSyncWork>(3, TimeUnit.HOURS)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.UNMETERED)
                            .setRequiresBatteryNotLow(true)
                            .build()
                    )
                    .setInputData(inputData)
                    .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                    .build()
            )
        }

        fun cancelManualWork(applicationContext: Context) {
            WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_WORK_ID)
        }

        fun cancelAutoWork(applicationContext: Context) {
            WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_PERIODIC_WORK_ID)
        }
    }

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds
        @IntoMap
        @WorkerKey(SaveSyncWork::class)
        abstract fun bindMyWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out ListenableWorker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<SaveSyncWork> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<SaveSyncWork>()
    }
}
