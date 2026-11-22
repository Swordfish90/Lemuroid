package com.swordfish.lemuroid.app.mobile.shared

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.main.MainActivity
import com.swordfish.lemuroid.app.shared.library.CoreUpdateBroadcastReceiver
import com.swordfish.lemuroid.app.shared.library.LibraryIndexBroadcastReceiver
import com.swordfish.lemuroid.app.shared.scraper.BatchScraperBroadcastReceiver

class NotificationsManager(private val applicationContext: Context) {
    fun gameRunningNotification(gameIntent: Intent): Notification {
        createDefaultNotificationChannel()

        val intent =
            Intent(gameIntent).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
        val contentIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val title = applicationContext.getString(R.string.game_running_notification_title_alternative)

        val builder =
            NotificationCompat.Builder(applicationContext, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lemuroid_tiny)
                .setContentTitle(title)
                .setContentText(applicationContext.getString(R.string.game_running_notification_message))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setVibrate(null)
                .setSound(null)
                .setContentIntent(contentIntent)

        return builder.build()
    }

    fun libraryIndexingNotification(): Notification {
        createDefaultNotificationChannel()

        val broadcastIntent = Intent(applicationContext, LibraryIndexBroadcastReceiver::class.java)
        val broadcastPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                applicationContext,
                0,
                broadcastIntent,
                PendingIntent.FLAG_IMMUTABLE,
            )

        val builder =
            NotificationCompat.Builder(applicationContext, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lemuroid_tiny)
                .setContentTitle(applicationContext.getString(R.string.library_index_notification_title))
                .setContentText(applicationContext.getString(R.string.library_index_notification_message))
                .setProgress(100, 0, true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(
                    NotificationCompat.Action(
                        null,
                        applicationContext.getString(R.string.cancel),
                        broadcastPendingIntent,
                    ),
                )

        return builder.build()
    }

    fun installingCoresNotification(): Notification {
        createDefaultNotificationChannel()

        val broadcastIntent = Intent(applicationContext, CoreUpdateBroadcastReceiver::class.java)
        val broadcastPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                applicationContext,
                0,
                broadcastIntent,
                PendingIntent.FLAG_IMMUTABLE,
            )

        val builder =
            NotificationCompat.Builder(applicationContext, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lemuroid_tiny)
                .setContentTitle(
                    applicationContext.getString(
                        com.swordfish.lemuroid.ext.R.string.installing_core_notification_title,
                    ),
                )
                .setContentText(
                    applicationContext.getString(
                        com.swordfish.lemuroid.ext.R.string.installing_core_notification_message,
                    ),
                )
                .setProgress(100, 0, true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(
                    NotificationCompat.Action(
                        null,
                        applicationContext.getString(R.string.cancel),
                        broadcastPendingIntent,
                    ),
                )

        return builder.build()
    }

    fun saveSyncNotification(): Notification {
        createDefaultNotificationChannel()

        val builder =
            NotificationCompat.Builder(applicationContext, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lemuroid_tiny)
                .setContentTitle(applicationContext.getString(R.string.save_sync_notification_title))
                .setContentText(applicationContext.getString(R.string.save_sync_notification_message))
                .setProgress(100, 0, true)
                .setPriority(NotificationCompat.PRIORITY_LOW)

        return builder.build()
    }

    fun scrapingProgressNotification(
        platformName: String,
        processed: Int,
        scraped: Int,
        total: Int,
        currentGameTitle: String,
        overallProcessed: Int? = null,
        overallTotal: Int? = null,
    ): Notification {
        createScraperNotificationChannel()

        val remaining = (total - processed).coerceAtLeast(0)

        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            SCRAPER_NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val cancelIntent = Intent(applicationContext, BatchScraperBroadcastReceiver::class.java)
        val cancelPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            SCRAPER_NOTIFICATION_ID,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val isDualProgress = overallProcessed != null && overallTotal != null
        val progressLabel = if (isDualProgress) {
            "$overallProcessed / $overallTotal"
        } else {
            "$processed / $total"
        }

        val expandedText = if (isDualProgress) {
            val overallRemaining = (overallTotal!! - overallProcessed!!).coerceAtLeast(0)
            applicationContext.getString(
                R.string.scraper_notification_expanded_dual,
                currentGameTitle,
                scraped,
                remaining,
                total,
                overallProcessed,
                overallTotal,
                overallRemaining,
            )
        } else {
            applicationContext.getString(
                R.string.scraper_notification_expanded,
                currentGameTitle,
                scraped,
                remaining,
                total,
            )
        }

        val title = if (isDualProgress) {
            applicationContext.getString(R.string.scraper_notification_all_platforms)
        } else {
            applicationContext.getString(
                R.string.scraper_notification_title,
                platformName,
            )
        }

        val progressMax = if (isDualProgress) overallTotal!! else total
        val progressCurrent = if (isDualProgress) overallProcessed!! else processed

        return NotificationCompat.Builder(applicationContext, SCRAPER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lemuroid_tiny)
            .setContentTitle(title)
            .setContentText(progressLabel)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setProgress(progressMax, progressCurrent, progressMax <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .addAction(
                NotificationCompat.Action(
                    null,
                    applicationContext.getString(R.string.cancel),
                    cancelPendingIntent,
                ),
            )
            .build()
    }

    fun scrapingCompleteNotification(
        platformName: String,
        scrapedCount: Int,
        totalCount: Int,
        isAllPlatforms: Boolean = false,
    ): Notification {
        createScraperNotificationChannel()

        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            SCRAPER_NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = if (isAllPlatforms) {
            applicationContext.getString(R.string.scraper_notification_complete_all_title)
        } else {
            applicationContext.getString(
                R.string.scraper_notification_complete_title,
                platformName,
            )
        }

        val message = if (isAllPlatforms) {
            applicationContext.getString(
                R.string.scraper_notification_complete_all_message,
                scrapedCount,
                totalCount,
            )
        } else {
            applicationContext.getString(
                R.string.scraper_notification_complete_message,
                scrapedCount,
                totalCount,
            )
        }

        return NotificationCompat.Builder(applicationContext, SCRAPER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lemuroid_tiny)
            .setContentTitle(title)
            .setContentText(message)
            .setOngoing(false)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun createDefaultNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_MIN
            val mChannel = NotificationChannel(DEFAULT_CHANNEL_ID, name, importance)
            val notificationManager =
                ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)

            notificationManager?.createNotificationChannel(mChannel)
        }
    }

    private fun createScraperNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.scraper_notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(SCRAPER_CHANNEL_ID, name, importance).apply {
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager =
                ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)

            notificationManager?.createNotificationChannel(mChannel)
        }
    }

    companion object {
        const val DEFAULT_CHANNEL_ID = "DEFAULT_CHANNEL_ID"
        const val SCRAPER_CHANNEL_ID = "SCRAPER_CHANNEL_ID"

        const val LIBRARY_INDEXING_NOTIFICATION_ID = 1
        const val SAVE_SYNC_NOTIFICATION_ID = 2
        const val GAME_RUNNING_NOTIFICATION_ID = 3
        const val CORE_INSTALL_NOTIFICATION_ID = 4
        const val SCRAPER_NOTIFICATION_ID = 5
        const val SCRAPER_COMPLETE_NOTIFICATION_ID = 6
    }
}
