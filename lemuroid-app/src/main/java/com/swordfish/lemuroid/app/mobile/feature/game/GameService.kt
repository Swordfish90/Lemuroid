package com.swordfish.lemuroid.app.mobile.feature.game

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import dagger.android.DaggerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.system.exitProcess

class GameService : DaggerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            awaitTermination()
            withContext(Dispatchers.Main) {
                ServiceCompat.stopForeground(this@GameService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
                exitProcess(0)
            }
        }
    }

    override fun onStartCommand(
        intent: Intent,
        flags: Int,
        startId: Int,
    ): Int {
        displayNotification(intent)
        return START_NOT_STICKY
    }

    private fun displayNotification(intent: Intent) {
        val gameIntent =
            intent.getParcelableExtra<Intent>(EXTRA_GAME_ACTIVITY_INTENT)
                ?: return
        val notification = NotificationsManager(applicationContext).gameRunningNotification(gameIntent)
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
        serviceScope.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        hideNotification()
    }

    companion object {
        private const val EXTRA_GAME_ACTIVITY_INTENT = "EXTRA_GAME_ACTIVITY_INTENT"

        private data class GameProcessTask(
            val task: suspend () -> Unit = {},
            val terminate: Boolean = false,
        )

        private val tasks = Channel<GameProcessTask>(capacity = Channel.BUFFERED)

        fun startService(
            context: Context,
            gameActivityIntent: Intent,
        ) {
            context.startService(
                Intent(context, GameService::class.java).apply {
                    putExtra(EXTRA_GAME_ACTIVITY_INTENT, gameActivityIntent)
                },
            )
        }

        fun schedule(task: suspend () -> Unit) {
            val result = tasks.trySend(GameProcessTask(task = task))
            Timber.i("GameService.schedule sent=%s", result.isSuccess)
        }

        fun requestTermination() {
            val result = tasks.trySend(GameProcessTask(terminate = true))
            Timber.i("GameService.requestTermination sent=%s", result.isSuccess)
        }

        private suspend fun awaitTermination() {
            for (task in tasks) {
                runCatching { task.task() }
                    .onFailure { Timber.e(it, "GameService task failed") }

                if (task.terminate) {
                    return
                }
            }
        }
    }
}
