package com.codebutler.retrograde.lib.injection

import androidx.work.Worker
import dagger.android.AndroidInjector

interface HasWorkerInjector {
    fun workerInjector(): AndroidInjector<Worker>
}
