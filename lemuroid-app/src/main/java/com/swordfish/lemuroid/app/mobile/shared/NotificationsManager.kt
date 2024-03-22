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
import com.swordfish.lemuroid.app.mobile.feature.game.GameActivity
import com.swordfish.lemuroid.app.shared.library.CoreUpdateBroadcastReceiver
import com.swordfish.lemuroid.app.shared.library.LibraryIndexBroadcastReceiver
import com.swordfish.lemuroid.lib.library.db.entity.Game

class NotificationsManager(private val applicationContext: Context) {
    fun gameRunningNotification(game: Game?): Notification {
        createDefaultNotificationChannel()

        val intent = Intent(applicationContext, GameActivity::class.java)
        val contentIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val title =
            game?.let {
                applicationContext.getString(R.string.game_running_notification_title, game.title)
            } ?: applicationContext.getString(R.string.game_running_notification_title_alternative)

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

    companion object {
        const val DEFAULT_CHANNEL_ID = "DEFAULT_CHANNEL_ID"

        const val LIBRARY_INDEXING_NOTIFICATION_ID = 1
        const val SAVE_SYNC_NOTIFICATION_ID = 2
        const val GAME_RUNNING_NOTIFICATION_ID = 3
        const val CORE_INSTALL_NOTIFICATION_ID = 4
    }
}
