package com.codebutler.retrograde.lib.injection

import androidx.work.ListenableWorker
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.Multibinds

@Module
abstract class AndroidWorkerInjectionModule {
    @Multibinds
    abstract fun workerInjectorFactories(): Map<Class<out ListenableWorker>, AndroidInjector.Factory<out ListenableWorker>>

    @Multibinds
    abstract fun workerInjectorFactoriesWithStringKeys(): Map<String, AndroidInjector.Factory<out ListenableWorker>>
}
