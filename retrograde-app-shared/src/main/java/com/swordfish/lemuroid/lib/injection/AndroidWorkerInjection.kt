package com.swordfish.lemuroid.lib.injection

import androidx.work.ListenableWorker

object AndroidWorkerInjection {
    fun inject(worker: ListenableWorker) {
        checkNotNull(worker) { "worker" }
        val application = worker.applicationContext
        if (application !is HasWorkerInjector) {
            throw RuntimeException(
                "${application.javaClass.canonicalName} does not " +
                    "implement ${HasWorkerInjector::class.java.canonicalName}",
            )
        }
        val factory =
            application.workerInjector()[worker.javaClass]
                ?: throw IllegalArgumentException(
                    "No injector factory bound for Class<${worker.javaClass.name}>",
                )
        @Suppress("UNCHECKED_CAST")
        val injector = factory as dagger.android.AndroidInjector.Factory<ListenableWorker>
        injector.create(worker).inject(worker)
    }
}
