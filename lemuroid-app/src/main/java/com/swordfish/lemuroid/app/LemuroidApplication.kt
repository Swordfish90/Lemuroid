package com.swordfish.lemuroid.app

import android.annotation.SuppressLint
import android.content.Context
import androidx.startup.AppInitializer
import androidx.work.ListenableWorker
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.android.material.color.DynamicColors
import com.swordfish.lemuroid.app.shared.covers.CoverUtils
import com.swordfish.lemuroid.app.shared.startup.GameProcessInitializer
import com.swordfish.lemuroid.app.shared.startup.MainProcessInitializer
import com.swordfish.lemuroid.app.utils.android.isMainProcess
import com.swordfish.lemuroid.ext.feature.context.ContextHandler
import com.swordfish.lemuroid.lib.injection.HasWorkerInjector
import com.swordfish.lemuroid.lib.injection.WorkerInjectorMap
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import javax.inject.Inject

class LemuroidApplication : DaggerApplication(), HasWorkerInjector, ImageLoaderFactory {
    @Inject
    @WorkerInjectorMap
    lateinit var workerInjector:
        Map<
            Class<out ListenableWorker>,
            @JvmSuppressWildcards AndroidInjector.Factory<out ListenableWorker>,
        >

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()

        val initializeComponent =
            if (isMainProcess()) {
                MainProcessInitializer::class.java
            } else {
                GameProcessInitializer::class.java
            }

        AppInitializer.getInstance(this).initializeComponent(initializeComponent)

        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        ContextHandler.attachBaseContext(base)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerLemuroidApplicationComponent.builder().create(this)
    }

    override fun workerInjector():
        Map<
            Class<out ListenableWorker>,
            @JvmSuppressWildcards AndroidInjector.Factory<out ListenableWorker>,
        > = workerInjector

    override fun newImageLoader(): ImageLoader {
        return CoverUtils.buildImageLoader(applicationContext)
    }
}
