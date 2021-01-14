package com.swordfish.lemuroid.app.shared.gamecrash

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.swordfish.lemuroid.BuildConfig

class GameCrashHandler(
    private val activity: Activity,
    private val systemHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val message = if (BuildConfig.DEBUG) {
            Log.getStackTraceString(throwable)
        } else {
            throwable.message
        }

        activity.startActivity(
            Intent(activity, GameCrashActivity::class.java).apply {
                putExtra(GameCrashActivity.EXTRA_MESSAGE, message)
            }
        )
        activity.finish()
        systemHandler?.uncaughtException(thread, throwable)
    }
}
