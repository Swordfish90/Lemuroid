package com.swordfish.lemuroid.lib.injection

import androidx.work.ListenableWorker
import dagger.android.AndroidInjector
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerInjectorMap

interface HasWorkerInjector {
    fun workerInjector():
        Map<
            Class<out ListenableWorker>,
            @JvmSuppressWildcards AndroidInjector.Factory<out ListenableWorker>,
        >
}
