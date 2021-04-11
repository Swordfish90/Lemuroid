package com.swordfish.lemuroid.app.mobile.feature.game

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import com.swordfish.lemuroid.lib.library.db.entity.Game

class GameService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val game = intent.extras?.getSerializable(EXTRA_GAME) as Game?
        displayNotification(game)
        return START_STICKY
    }

    private fun displayNotification(game: Game?) {
        val notification = NotificationsManager(applicationContext).gameRunningNotification(game)
        startForeground(NotificationsManager.GAME_RUNNING_NOTIFICATION_ID, notification)
    }

    private fun hideNotification() {
        NotificationManagerCompat.from(this).cancel(NotificationsManager.GAME_RUNNING_NOTIFICATION_ID)
    }

    override fun onDestroy() {
        hideNotification()
    }

    companion object {
        private val EXTRA_GAME = "EXTRA_GAME"

        fun startService(context: Context, game: Game): Intent {
            val result = Intent(context, GameService::class.java).apply {
                putExtra(EXTRA_GAME, game)
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(result)
            } else {
                context.startService(result)
            }

            return result
        }

        fun stopService(context: Context, intent: Intent) {
            context.stopService(intent)
        }
    }
}
