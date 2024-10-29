package com.swordfish.lemuroid.app.mobile.feature.game

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import com.swordfish.lemuroid.lib.library.db.entity.Game

class GameService : Service() {
    private val binder = NotificationServiceBinder()

    inner class NotificationServiceBinder : Binder() {
        fun getService(): GameService {
            return this@GameService
        }
    }

    class GameServiceController(
        private val intent: Intent,
        private val connection: ServiceConnection,
    ) {
        fun stopService(context: Context) {
            context.unbindService(connection)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(
        intent: Intent,
        flags: Int,
        startId: Int,
    ): Int {
        val game =
            kotlin.runCatching {
                intent.extras?.getSerializable(EXTRA_GAME) as Game?
            }.getOrNull()

        displayNotification(game)
        return START_NOT_STICKY
    }

    private fun displayNotification(game: Game?) {
        val notification = NotificationsManager(applicationContext).gameRunningNotification(game)
        ServiceCompat.startForeground(
            this,
            NotificationsManager.GAME_RUNNING_NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
    }

    private fun hideNotification() {
        NotificationManagerCompat.from(this).cancel(NotificationsManager.GAME_RUNNING_NOTIFICATION_ID)
    }

    override fun onDestroy() {
        hideNotification()
    }

    companion object {
        private val EXTRA_GAME = "EXTRA_GAME"

        fun startService(
            context: Context,
            game: Game,
        ): GameServiceController {
            val intent =
                Intent(context, GameService::class.java).apply {
                    putExtra(EXTRA_GAME, game)
                }

            val connection =
                object : ServiceConnection {
                    override fun onServiceConnected(
                        name: ComponentName?,
                        service: IBinder?,
                    ) {
                        // Do nothing
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        // Do nothing
                    }
                }

            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            context.startService(intent)

            return GameServiceController(intent, connection)
        }

        fun stopService(
            context: Context,
            serviceController: GameServiceController?,
        ): GameServiceController? {
            serviceController?.stopService(context)
            return null
        }
    }
}
