package com.swordfish.lemuroid.lib.injection

import androidx.work.ListenableWorker
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.Multibinds

private typealias Worker = Class<out ListenableWorker>
private typealias WorkerFactory = AndroidInjector.Factory<out ListenableWorker>

@Module
abstract class AndroidWorkerInjectionModule {
    @Multibinds
    abstract fun workerInjectorFactories(): Map<Worker, WorkerFactory>

    @Multibinds
    abstract fun workerInjectorFactoriesWithStringKeys(): Map<String, WorkerFactory>
}
