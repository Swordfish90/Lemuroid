package com.swordfish.lemuroid.app.shared.scraper

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.swordfish.lemuroid.lib.library.MetaSystemID

object BatchScraperScheduler {
    val BATCH_SCRAPER_WORK_ID: String = BatchScraperWork::class.java.simpleName

    fun scheduleBatchScrape(
        applicationContext: Context,
        metaSystemID: MetaSystemID,
        overwrite: Boolean,
    ) {
        val inputData = workDataOf(
            BatchScraperWork.KEY_META_SYSTEM_ID to metaSystemID.name,
            BatchScraperWork.KEY_OVERWRITE to overwrite,
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<BatchScraperWork>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(
                BATCH_SCRAPER_WORK_ID,
                ExistingWorkPolicy.REPLACE,
                request,
            )
            .enqueue()
    }

    fun scheduleBatchScrapeAll(
        applicationContext: Context,
        overwrite: Boolean,
    ) {
        val inputData = workDataOf(
            BatchScraperWork.KEY_META_SYSTEM_ID to BatchScraperWork.ALL_SYSTEMS_KEY,
            BatchScraperWork.KEY_OVERWRITE to overwrite,
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<BatchScraperWork>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(
                BATCH_SCRAPER_WORK_ID,
                ExistingWorkPolicy.REPLACE,
                request,
            )
            .enqueue()
    }

    fun cancelBatchScrape(applicationContext: Context) {
        BatchScraperManager.cancelActive()
        WorkManager.getInstance(applicationContext).cancelUniqueWork(BATCH_SCRAPER_WORK_ID)
    }
}
