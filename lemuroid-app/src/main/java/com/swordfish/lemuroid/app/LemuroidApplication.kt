package com.swordfish.lemuroid.app

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import com.swordfish.lemuroid.BuildConfig
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import com.swordfish.lemuroid.lib.injection.HasWorkerInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject

class LemuroidApplication : DaggerApplication(), HasWorkerInjector {
    companion object {
        fun get(context: Context) = context.applicationContext as LemuroidApplication
    }

    /*@Inject
    lateinit var rxTimberTree: RxTimberTree
    @Inject
    lateinit var rxPrefs: RxSharedPreferences
    @Inject
    lateinit var gdriveStorageProvider: GDriveStorageProvider*/
    @Inject
    lateinit var workerInjector: DispatchingAndroidInjector<ListenableWorker>

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()

        initializeWorkManager()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // var isPlanted = false
        /* rxPrefs.getBoolean(getString(R.string.pref_key_flags_logging)).asObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { value ->
                    gdriveStorageProvider.loggingEnabled = value
                    if (value) {
                        Timber.plant(rxTimberTree)
                        isPlanted = true
                    } else {
                        if (isPlanted) {
                            Timber.uproot(rxTimberTree)
                            isPlanted = false
                        }
                    }
                }*/
    }

    private fun initializeWorkManager() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

        WorkManager.initialize(this, config)

        if (isMainProcess()) {
            SaveSyncWork.enqueueUniqueWork(applicationContext)
        }
    }

    private fun isMainProcess(): Boolean {
        return retrieveProcessName() == packageName
    }

    private fun retrieveProcessName(): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return getProcessName()
        }

        val currentPID = Process.myPid()
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return manager.runningAppProcesses
            .firstOrNull { it.pid == currentPID }
            ?.processName
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerLemuroidApplicationComponent.builder().create(this)
    }

    override fun workerInjector(): AndroidInjector<ListenableWorker> = workerInjector
}
