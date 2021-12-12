package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import com.swordfish.lemuroid.lib.injection.AndroidWorkerInjection
import com.swordfish.lemuroid.lib.injection.WorkerKey
import com.swordfish.lemuroid.lib.library.LemuroidLibrary
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class LibraryIndexWork(context: Context, workerParams: WorkerParameters) :
    RxWorker(context, workerParams) {

    @Inject lateinit var lemuroidLibrary: LemuroidLibrary

    override fun createWork(): Single<Result> {
        AndroidWorkerInjection.inject(this)

        val notificationsManager = NotificationsManager(applicationContext)

        val foregroundInfo = ForegroundInfo(
            NotificationsManager.LIBRARY_INDEXING_NOTIFICATION_ID,
            notificationsManager.libraryIndexingNotification()
        )

        setForegroundAsync(foregroundInfo)
        return lemuroidLibrary.indexLibrary()
            .toSingleDefault(Result.success())
            .doOnError { Timber.e(it, "Library indexing failed with exception: $it") }
            .doFinally { LibraryIndexScheduler.scheduleCoreUpdate(applicationContext) }
            .onErrorReturn { Result.success() } // We need to return success or the Work chain will die forever.
    }

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds
        @IntoMap
        @WorkerKey(LibraryIndexWork::class)
        abstract fun bindMyWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out ListenableWorker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<LibraryIndexWork> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<LibraryIndexWork>()
    }
}
