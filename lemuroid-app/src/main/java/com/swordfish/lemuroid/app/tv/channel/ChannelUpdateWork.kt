package com.swordfish.lemuroid.app.tv.channel

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.swordfish.lemuroid.lib.injection.AndroidWorkerInjection
import com.swordfish.lemuroid.lib.injection.WorkerKey
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ChannelUpdateWork(context: Context, workerParams: WorkerParameters) :
    RxWorker(context, workerParams) {

    @Inject lateinit var channelHandler: ChannelHandler

    override fun createWork(): Single<Result> {
        AndroidWorkerInjection.inject(this)

        return channelHandler
            .update()
            .doOnError { e -> Timber.e(e, "Error in channel update") }
            .subscribeOn(Schedulers.io())
            .onErrorComplete()
            .andThen(Single.just(Result.success()))
    }

    companion object {
        private val UNIQUE_WORK_ID: String = ChannelUpdateWork::class.java.simpleName

        fun enqueue(applicationContext: Context) {
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ChannelUpdateWork>().build()
            )
        }

        fun cancel(applicationContext: Context) {
            WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_WORK_ID)
        }
    }

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds
        @IntoMap
        @WorkerKey(ChannelUpdateWork::class)
        abstract fun bindMyWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out ListenableWorker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<ChannelUpdateWork> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<ChannelUpdateWork>()
    }
}
