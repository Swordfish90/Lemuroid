package com.swordfish.lemuroid.app.shared.startup

import android.content.Context
import android.os.StrictMode
import androidx.startup.Initializer
import com.swordfish.lemuroid.BuildConfig
import com.swordfish.lemuroid.app.shared.diagnostics.AppDiagnosticLog
import timber.log.Timber

class DebugInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Timber.plant(AppDiagnosticLog.PersistentTree(context))
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            enableStrictMode()
        }
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build(),
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
