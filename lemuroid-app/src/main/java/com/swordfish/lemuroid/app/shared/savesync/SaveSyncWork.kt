package com.swordfish.lemuroid.app.shared.savesync

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import com.swordfish.lemuroid.ext.feature.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.injection.AndroidWorkerInjection
import com.swordfish.lemuroid.lib.injection.WorkerKey
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
    @Inject lateinit var settingsManager: SettingsManager

    override fun createWork(): Single<Result> {
        AndroidWorkerInjection.inject(this)

        if (!settingsManager.syncSaves || !saveSyncManager.isSupported() || !saveSyncManager.isConfigured())
            return Single.just(Result.success())

        val notificationsManager = NotificationsManager(applicationContext)

        val foregroundInfo = ForegroundInfo(
            NotificationsManager.SAVE_SYNC_NOTIFICATION_ID,
            notificationsManager.saveSyncNotification()
        )
        setForegroundAsync(foregroundInfo)

        return saveSyncManager
            .sync(settingsManager.syncStates)
            .doOnError { e -> Timber.e(e, "Error in saves sync") }
            .subscribeOn(Schedulers.io())
            .onErrorComplete()
            .andThen(Single.just(Result.success()))
    }

    companion object {
        val UNIQUE_WORK_ID: String = SaveSyncWork::class.java.simpleName
        val UNIQUE_PERIODIC_WORK_ID: String = SaveSyncWork::class.java.simpleName + "Periodic"

        fun enqueueUniqueWork(applicationContext: Context, duration: Long = 0, timeUnit: TimeUnit = TimeUnit.SECONDS) {
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SaveSyncWork>()
                    .setInitialDelay(duration, timeUnit).build()
            )
        }

        fun cancelUniqueWork(applicationContext: Context) {
            WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_WORK_ID)
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
