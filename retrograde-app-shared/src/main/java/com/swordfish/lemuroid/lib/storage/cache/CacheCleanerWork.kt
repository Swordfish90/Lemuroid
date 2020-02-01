package com.swordfish.lemuroid.lib.storage.cache

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import io.reactivex.Completable
import io.reactivex.Single

class CacheCleanerWork(context: Context, workerParams: WorkerParameters) : RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> {
        return createCleanCacheCompletable(applicationContext)
            .onErrorComplete()
            .toSingleDefault(Result.success())
    }

    private fun createCleanCacheCompletable(context: Context): Completable {
        val cacheCleaner = CacheCleaner()
        val optimalCacheSize = cacheCleaner.getOptimalCacheSize()
        return cacheCleaner.clean(context, optimalCacheSize)
    }

    companion object {
        private val UNIQUE_WORK_ID: String = CacheCleanerWork::class.java.simpleName

        fun enqueueUniqueWork(applicationContext: Context) {
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.APPEND,
                OneTimeWorkRequestBuilder<CacheCleanerWork>().build()
            )
        }
    }
}
