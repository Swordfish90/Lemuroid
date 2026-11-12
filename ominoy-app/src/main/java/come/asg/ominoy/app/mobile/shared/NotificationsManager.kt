package come.asg.ominoy.app.mobile.shared

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import come.asg.ominoy.R
import come.asg.ominoy.app.shared.library.LibraryIndexBroadcastReceiver
import come.asg.ominoy.app.shared.game.RetroAchievementsSyncCancelReceiver

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
                .setSmallIcon(R.drawable.ic_ominoy_tiny)
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
                .setSmallIcon(R.drawable.ic_ominoy_tiny)
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

    fun saveSyncNotification(): Notification {
        createDefaultNotificationChannel()

        val builder =
            NotificationCompat.Builder(applicationContext, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_ominoy_tiny)
                .setContentTitle(applicationContext.getString(R.string.save_sync_notification_title))
                .setContentText(applicationContext.getString(R.string.save_sync_notification_message))
                .setProgress(100, 0, true)
                .setPriority(NotificationCompat.PRIORITY_LOW)

        return builder.build()
    }

    fun retroAchievementsSyncNotification(
        completed: Int = 0,
        total: Int = 0,
        current: String = "",
        artworkCompleted: Int = 0,
        artworkTotal: Int = 0,
    ): Notification {
        createRetroAchievementsSyncChannel()
        val cancelIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            Intent(applicationContext, RetroAchievementsSyncCancelReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val gameProgress = if (total > 0) "$completed of $total games" else "Preparing game list…"
        val currentGame = current.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()
        val artworkProgress = if (artworkTotal > 0) " · Artwork $artworkCompleted of $artworkTotal" else ""
        val text = "$gameProgress$currentGame$artworkProgress"
        val progressMax = if (artworkTotal > 0) artworkTotal else total.coerceAtLeast(1)
        val progressValue = if (artworkTotal > 0) artworkCompleted else completed
        return NotificationCompat.Builder(applicationContext, RETRO_ACHIEVEMENTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_ominoy_tiny)
            .setContentTitle("RetroAchievements sync")
            .setContentText(text)
            .setProgress(progressMax, progressValue.coerceIn(0, progressMax), total <= 0 && artworkTotal <= 0)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(NotificationCompat.Action(null, applicationContext.getString(R.string.cancel), cancelIntent))
            .build()
    }

    fun retroAchievementsCompleteNotification(synced: Int, skipped: Int): Notification {
        createRetroAchievementsSyncChannel()
        return NotificationCompat.Builder(applicationContext, RETRO_ACHIEVEMENTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_ominoy_tiny)
            .setContentTitle("RetroAchievements sync complete")
            .setContentText("$synced games synced${if (skipped > 0) ", $skipped not found" else ""}")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showRetroAchievementsNotification(notification: Notification) {
        ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)
            ?.notify(RETRO_ACHIEVEMENTS_NOTIFICATION_ID, notification)
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

    private fun createRetroAchievementsSyncChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RETRO_ACHIEVEMENTS_CHANNEL_ID,
                "RetroAchievements sync",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Silent progress updates while syncing achievement data and artwork"
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }
            ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val DEFAULT_CHANNEL_ID = "DEFAULT_CHANNEL_ID"

        const val LIBRARY_INDEXING_NOTIFICATION_ID = 1
        const val SAVE_SYNC_NOTIFICATION_ID = 2
        const val GAME_RUNNING_NOTIFICATION_ID = 3
        const val RETRO_ACHIEVEMENTS_NOTIFICATION_ID = 5
        const val RETRO_ACHIEVEMENTS_CHANNEL_ID = "RETRO_ACHIEVEMENTS_SYNC"
    }
}
