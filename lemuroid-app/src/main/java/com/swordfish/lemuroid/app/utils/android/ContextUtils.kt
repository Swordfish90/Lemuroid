package com.swordfish.lemuroid.app.utils.android

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import dagger.android.support.DaggerApplication

fun Context.isMainProcess(): Boolean {
    return retrieveProcessName(this) == this.packageName
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
