package com.codebutler.retrograde.app

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Worker
import com.codebutler.retrograde.BuildConfig
import com.codebutler.retrograde.app.DaggerRetrogradeApplicationComponent
import com.codebutler.retrograde.lib.injection.HasWorkerInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import javax.inject.Inject

class RetrogradeApplication : DaggerApplication(), HasWorkerInjector {
    companion object {
        init {
            if (BuildConfig.DEBUG) {
                System.setProperty("jna.debug_load", "true")
                System.setProperty("jna.debug_load.jna", "true")
                System.setProperty("jna.dump_memory", "true")
                System.setProperty("jna.nosys", "false")
                System.setProperty("jna.noclasspath", "true")
            }
        }
        fun get(context: Context) = context.applicationContext as RetrogradeApplication
    }

    /*@Inject
    lateinit var rxTimberTree: RxTimberTree
    @Inject
    lateinit var rxPrefs: RxSharedPreferences
    @Inject
    lateinit var gdriveStorageProvider: GDriveStorageProvider*/
    @Inject
    lateinit var workerInjector: DispatchingAndroidInjector<Worker>

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } /*else {
            Bugsnag.init(this)
        }*/

        //var isPlanted = false
        /*rxPrefs.getBoolean(getString(R.string.pref_key_flags_logging)).asObservable()
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

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerRetrogradeApplicationComponent.builder().create(this)
    }

    override fun workerInjector(): AndroidInjector<Worker> = workerInjector
}
