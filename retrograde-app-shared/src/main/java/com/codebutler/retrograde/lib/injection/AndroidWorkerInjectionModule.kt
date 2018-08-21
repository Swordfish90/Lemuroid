package com.codebutler.retrograde.lib.injection

import androidx.work.Worker
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.Multibinds

@Module
abstract class AndroidWorkerInjectionModule {
    @Multibinds
    abstract fun workerInjectorFactories(): Map<Class<out Worker>, AndroidInjector.Factory<out Worker>>

    @Multibinds
    abstract fun workerInjectorFactoriesWithStringKeys(): Map<String, AndroidInjector.Factory<out Worker>>
}
