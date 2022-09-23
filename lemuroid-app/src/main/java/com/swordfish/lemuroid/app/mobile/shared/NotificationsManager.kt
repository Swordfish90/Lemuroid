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
import com.swordfish.lemuroid.app.shared.library.LibraryIndexBroadcastReceiver
import com.swordfish.lemuroid.lib.library.db.entity.Game

class NotificationsManager(private val applicationContext: Context) {

    fun gameRunningNotification(game: Game?): Notification {
        createNotificationChannels()

        val intent = Intent(applicationContext, GameActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = game?.let {
            applicationContext.getString(R.string.game_running_notification_title, game.title)
        } ?: applicationContext.getString(R.string.game_running_notification_title_alternative)

        val builder = NotificationCompat.Builder(applicationContext, GAME_RUNNING_CHANNEL_ID)
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
        createNotificationChannels()

        val broadcastIntent = Intent(applicationContext, LibraryIndexBroadcastReceiver::class.java)
        val broadcastPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            broadcastIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, LIBRARY_INDEXING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lemuroid_tiny)
            .setContentTitle(applicationContext.getString(R.string.library_index_notification_title))
            .setContentText(applicationContext.getString(R.string.library_index_notification_message))
            .setProgress(100, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                NotificationCompat.Action(
                    null,
                    applicationContext.getString(R.string.cancel),
                    broadcastPendingIntent
                )
            )

        return builder.build()
    }

    fun installingCoresNotification(): Notification {
        createNotificationChannels()

        val builder = NotificationCompat.Builder(applicationContext, CORE_UPDATE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lemuroid_tiny)
            .setContentTitle(applicationContext.getString(R.string.installing_core_notification_title))
            .setContentText(applicationContext.getString(R.string.installing_core_notification_message))
            .setProgress(100, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        return builder.build()
    }

    fun saveSyncNotification(): Notification {
        createSyncNotificationChannels()

        val builder = NotificationCompat.Builder(applicationContext, SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lemuroid_tiny)
            .setContentTitle(applicationContext.getString(R.string.save_sync_notification_title))
            .setContentText(applicationContext.getString(R.string.save_sync_notification_message))
            .setProgress(100, 0, true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        return builder.build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager =
                ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)

            var name = applicationContext.getString(R.string.notification_channel_name_game_running)
            var importance = NotificationManager.IMPORTANCE_MIN
            var mChannel = NotificationChannel(GAME_RUNNING_CHANNEL_ID, name, importance)
            mChannel.description =  applicationContext.getString(R.string.notification_channel_description_game_running)

            notificationManager?.createNotificationChannel(mChannel)

            name = applicationContext.getString(R.string.notification_channel_name_indexing)
            importance = NotificationManager.IMPORTANCE_MIN
            mChannel = NotificationChannel(LIBRARY_INDEXING_CHANNEL_ID, name, importance)
            mChannel.description =  applicationContext.getString(R.string.notification_channel_description_indexing)

            notificationManager?.createNotificationChannel(mChannel)


            name = applicationContext.getString(R.string.notification_channel_name_core_update)
            importance = NotificationManager.IMPORTANCE_MIN
            mChannel = NotificationChannel(CORE_UPDATE_CHANNEL_ID, name, importance)
            mChannel.description =  applicationContext.getString(R.string.notification_channel_description_core_update)

            notificationManager?.createNotificationChannel(mChannel)

            notificationManager?.deleteNotificationChannel(DEFAULT_CHANNEL_ID)
        }
    }

    private fun createSyncNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)

            var name = applicationContext.getString(R.string.notification_channel_name_sync)
            var importance = NotificationManager.IMPORTANCE_MIN
            var mChannel = NotificationChannel(SYNC_CHANNEL_ID, name, importance)
            mChannel.description =  applicationContext.getString(R.string.notification_channel_description_sync)

            notificationManager?.createNotificationChannel(mChannel)
        }
    }

    companion object {
        const val GAME_RUNNING_CHANNEL_ID = "GAME_RUNNING_CHANNEL_ID"
        const val LIBRARY_INDEXING_CHANNEL_ID = "LIBRARY_INDEXING_CHANNEL_ID"
        const val SYNC_CHANNEL_ID = "SYNC_CHANNEL_ID"
        const val CORE_UPDATE_CHANNEL_ID = "CORE_UPDATE_CHANNEL_ID"

        @Deprecated(message="Dont use! Use the proper channel. This ID is kept to remove the old channel.")
        const val DEFAULT_CHANNEL_ID = "DEFAULT_CHANNEL_ID"


        const val LIBRARY_INDEXING_NOTIFICATION_ID = 1
        const val SAVE_SYNC_NOTIFICATION_ID = 2
        const val GAME_RUNNING_NOTIFICATION_ID = 3
        const val CORE_INSTALL_NOTIFICATION_ID = 4
    }
}
