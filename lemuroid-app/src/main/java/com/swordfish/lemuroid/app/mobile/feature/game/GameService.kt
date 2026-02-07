package com.swordfish.lemuroid.app.mobile.feature.game

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import com.swordfish.lemuroid.app.shared.game.saves.AutoSaveCoordinator
import dagger.android.DaggerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.exitProcess

class GameService : DaggerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var autoSaveCoordinator: AutoSaveCoordinator

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        displayNotification()
    }

    override fun onStartCommand(
        intent: Intent,
        flags: Int,
        startId: Int,
    ): Int {
        serviceScope.launch {
            processPending()
        }
        return START_NOT_STICKY
    }

    private fun displayNotification() {
        val notification = NotificationsManager(applicationContext).gameRunningNotification()
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

    private suspend fun processPending() {
        val payload = autoSaveCoordinator.popPending()
        if (payload != null) {
            autoSaveCoordinator.write(payload)
        }
        finishServiceIfNeeded()
    }

    private suspend fun finishServiceIfNeeded() {
        if (!autoSaveCoordinator.shouldStop() || autoSaveCoordinator.hasPending()) {
            return
        }

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        hideNotification()
        delay(500)
        stopSelf()
        exitProcess(0)
    }

    companion object {
        fun startService(context: Context) {
            context.startService(Intent(context, GameService::class.java))
        }
    }
}
