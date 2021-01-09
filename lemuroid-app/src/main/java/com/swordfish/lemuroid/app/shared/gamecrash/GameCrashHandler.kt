package com.swordfish.lemuroid.app.shared.gamecrash

import android.content.Context
import android.content.Intent
import android.util.Log
import com.swordfish.lemuroid.BuildConfig

class GameCrashHandler(
    private val appContext: Context,
    private val systemHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val message = if (BuildConfig.DEBUG) {
            Log.getStackTraceString(throwable)
        } else {
            throwable.message
        }

        appContext.startActivity(Intent(appContext, GameCrashActivity::class.java).apply {
            putExtra(GameCrashActivity.EXTRA_MESSAGE, message)
        })
        systemHandler?.uncaughtException(thread, throwable)
    }
}
