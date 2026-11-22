package com.swordfish.lemuroid.app.shared.scraper

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import com.swordfish.lemuroid.app.utils.android.createSyncForegroundInfo
import com.swordfish.lemuroid.lib.injection.AndroidWorkerInjection
import com.swordfish.lemuroid.lib.injection.WorkerKey
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class BatchScraperWork(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var settingsManager: SettingsManager

    override suspend fun doWork(): Result {
        AndroidWorkerInjection.inject(this)

        val metaSystemName = inputData.getString(KEY_META_SYSTEM_ID) ?: return Result.failure()
        val isAllPlatforms = metaSystemName == ALL_SYSTEMS_KEY
        val metaSystemID = if (isAllPlatforms) null else runCatching { MetaSystemID.valueOf(metaSystemName) }.getOrNull()
        if (!isAllPlatforms && metaSystemID == null) return Result.failure()

        val overwrite = inputData.getBoolean(KEY_OVERWRITE, false) && runAttemptCount == 0

        val notificationsManager = NotificationsManager(applicationContext)
        val initialTitle = if (isAllPlatforms) {
            applicationContext.getString(com.swordfish.lemuroid.R.string.scraper_notification_all_platforms)
        } else {
            applicationContext.getString(metaSystemID!!.titleResId)
        }

        val initialNotification = notificationsManager.scrapingProgressNotification(
            platformName = initialTitle,
            processed = 0,
            scraped = 0,
            total = 0,
            currentGameTitle = "",
            overallProcessed = if (isAllPlatforms) 0 else null,
            overallTotal = if (isAllPlatforms) 0 else null,
        )
        runCatching {
            setForeground(
                createSyncForegroundInfo(
                    NotificationsManager.SCRAPER_NOTIFICATION_ID,
                    initialNotification,
                ),
            )
        }

        val batchScraperManager = BatchScraperManager(
            appContext = applicationContext,
            retrogradeDb = retrogradeDb,
            settingsManager = settingsManager,
        )

        return try {
            withContext(Dispatchers.IO) {
                if (isAllPlatforms) {
                    batchScraperManager.scrapeAllPlatforms(overwrite)
                } else {
                    batchScraperManager.scrapePlatform(metaSystemID!!, overwrite)
                }
            }
            Result.success()
        } catch (e: CancellationException) {
            Timber.i("BatchScraperWork cancelled for $metaSystemName")
            throw e
        } catch (e: Exception) {
            Timber.e(e, "BatchScraperWork failed for $metaSystemName")
            Result.retry()
        }
    }

    companion object {
        const val KEY_META_SYSTEM_ID = "meta_system_id"
        const val KEY_OVERWRITE = "overwrite"
        const val ALL_SYSTEMS_KEY = "ALL"
    }

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds
        @IntoMap
        @WorkerKey(BatchScraperWork::class)
        abstract fun bindMyWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out ListenableWorker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<BatchScraperWork> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<BatchScraperWork>()
    }
}
