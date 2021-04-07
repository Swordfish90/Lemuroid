package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.swordfish.lemuroid.app.mobile.shared.NotificationsManager
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.injection.AndroidWorkerInjection
import com.swordfish.lemuroid.lib.injection.WorkerKey
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

class CoreUpdateWork(context: Context, workerParams: WorkerParameters) :
    RxWorker(context, workerParams) {

    @Inject lateinit var retrogradeDatabase: RetrogradeDatabase
    @Inject lateinit var coreUpdater: CoreUpdater
    @Inject lateinit var coresSelection: CoresSelection

    override fun createWork(): Single<Result> {
        AndroidWorkerInjection.inject(this)

        Timber.i("Starting core update/install work")

        val notificationsManager = NotificationsManager(applicationContext)

        val foregroundInfo = ForegroundInfo(
            NotificationsManager.CORE_INSTALL_NOTIFICATION_ID,
            notificationsManager.installingCoresNotification()
        )

        setForegroundAsync(foregroundInfo)

        return retrogradeDatabase.gameDao()
            .rxSelectSystems()
            .firstOrError()
            .flatMap { systemIds ->
                Observable.fromIterable(systemIds)
                    .map { GameSystem.findById(it) }
                    .flatMapSingle { coresSelection.getCoreConfigForSystem(it) }
                    .map { it.coreID }
                    .toList()
            }
            .flatMapCompletable { coreUpdater.downloadCores(applicationContext, it) }
            .doOnError { Timber.e(it, "Core update work failed with exception: $it") }
            .toSingleDefault(Result.success())
            .onErrorReturn { Result.success() }
    }

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds
        @IntoMap
        @WorkerKey(CoreUpdateWork::class)
        abstract fun bindMyWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out ListenableWorker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<CoreUpdateWork> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<CoreUpdateWork>()
    }
}
