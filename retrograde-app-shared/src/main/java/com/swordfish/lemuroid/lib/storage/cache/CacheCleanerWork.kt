package com.swordfish.lemuroid.lib.storage.cache

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class CacheCleanerWork(context: Context, workerParams: WorkerParameters) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        val cleanCompletable = if (inputData.getBoolean(CLEAN_EVERYTHING, false)) {
            createCleanAllCompletable(applicationContext)
        } else {
            createCleanLRUCompletable(applicationContext)
        }
        return cleanCompletable.subscribeOn(Schedulers.io()).onErrorComplete().toSingleDefault(Result.success())
    }

    private fun createCleanLRUCompletable(context: Context): Completable {
        val optimalCacheSize = CacheCleaner.getOptimalCacheSize()
        return CacheCleaner.clean(context, optimalCacheSize)
    }

    private fun createCleanAllCompletable(context: Context): Completable {
        return CacheCleaner.cleanAll(context)
    }

    companion object {
        private val UNIQUE_WORK_ID: String = CacheCleanerWork::class.java.simpleName

        private val CLEAN_EVERYTHING: String = "CLEAN_EVERYTHING"

        fun enqueueCleanCacheLRU(applicationContext: Context) {
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.APPEND,
                OneTimeWorkRequestBuilder<CacheCleanerWork>().build()
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
                    .build()
            )
        }
    }
}
