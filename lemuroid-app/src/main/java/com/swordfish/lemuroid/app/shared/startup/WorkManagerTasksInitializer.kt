package com.swordfish.lemuroid.app.shared.startup

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import androidx.startup.Initializer
import androidx.work.WorkManagerInitializer
import com.swordfish.lemuroid.app.shared.library.LibraryIndexScheduler
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import dagger.android.support.DaggerApplication
import timber.log.Timber

class WorkManagerTasksInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Timber.i("Requested initialization of WorkManager tasks")
        if (isMainProcess(context)) {
            Timber.i("Running initial WorkManager tasks")
            SaveSyncWork.enqueueAutoWork(context, 0)
            LibraryIndexScheduler.scheduleCoreUpdate(context)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(WorkManagerInitializer::class.java, DebugInitializer::class.java)
    }

    private fun isMainProcess(context: Context): Boolean {
        return retrieveProcessName(context) == context.packageName
    }

    private fun retrieveProcessName(context: Context): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return DaggerApplication.getProcessName()
        }

        val currentPID = Process.myPid()
        val manager = context.getSystemService(DaggerApplication.ACTIVITY_SERVICE) as ActivityManager
        return manager.runningAppProcesses
            .firstOrNull { it.pid == currentPID }
            ?.processName
    }
}
