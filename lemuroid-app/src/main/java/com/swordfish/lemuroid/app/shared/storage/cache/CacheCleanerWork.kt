package com.swordfish.lemuroid.app.shared.storage.cache

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.lib.injection.AndroidWorkerInjection
import com.swordfish.lemuroid.lib.injection.WorkerKey
import com.swordfish.lemuroid.lib.storage.cache.CacheCleaner
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import timber.log.Timber
import javax.inject.Inject

class CacheCleanerWork(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    @Inject
    lateinit var settingsManager: SettingsManager

    override suspend fun doWork(): Result {
        AndroidWorkerInjection.inject(this)

        try {
            performCleaning()
        } catch (e: Throwable) {
            Timber.e(e, "Error while clearing cache")
        }

        return Result.success()
    }

    private suspend fun performCleaning() {
        if (inputData.getBoolean(CLEAN_EVERYTHING, false)) {
            cleanAll(applicationContext)
        } else {
            cleanLRU(applicationContext)
        }
    }

    private suspend fun cleanLRU(context: Context) {
        val size = settingsManager.cacheSizeBytes().toLong()
        CacheCleaner.clean(context, size)
    }

    private suspend fun cleanAll(context: Context) {
        return CacheCleaner.cleanAll(context)
    }

    companion object {
        private val UNIQUE_WORK_ID: String = CacheCleanerWork::class.java.simpleName

        private const val CLEAN_EVERYTHING: String = "CLEAN_EVERYTHING"

        fun enqueueCleanCacheLRU(applicationContext: Context) {
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.APPEND,
                OneTimeWorkRequestBuilder<CacheCleanerWork>().build(),
            )
        }

        fun cancelCleanCacheLRU(applicationContext: Context) {
            WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_WORK_ID)
        }

        fun enqueueCleanCacheAll(applicationContext: Context) {
            val inputData: Data = workDataOf(CLEAN_EVERYTHING to true)

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.APPEND,
                OneTimeWorkRequestBuilder<CacheCleanerWork>()
                    .setInputData(inputData)
                    .build(),
            )
        }
    }

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds
        @IntoMap
        @WorkerKey(CacheCleanerWork::class)
        abstract fun bindMyWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out ListenableWorker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<CacheCleanerWork> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<CacheCleanerWork>()
    }
}
