package come.asg.ominoy.app.shared.library

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import come.asg.ominoy.app.mobile.shared.NotificationsManager
import come.asg.ominoy.app.shared.game.RetroAchievementsSyncWork
import come.asg.ominoy.app.utils.android.createSyncForegroundInfo
import come.asg.ominoy.lib.injection.AndroidWorkerInjection
import come.asg.ominoy.lib.injection.WorkerKey
import come.asg.ominoy.lib.library.OminoyLibrary
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class LibraryIndexWork(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    @Inject
    lateinit var ominoyLibrary: OminoyLibrary

    override suspend fun doWork(): Result {
        AndroidWorkerInjection.inject(this)

        val notificationsManager = NotificationsManager(applicationContext)

        val foregroundInfo =
            createSyncForegroundInfo(
                NotificationsManager.LIBRARY_INDEXING_NOTIFICATION_ID,
                notificationsManager.libraryIndexingNotification(),
            )

        setForegroundAsync(foregroundInfo)

        val result =
            withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    ominoyLibrary.indexLibrary()
                }
            }

        result.exceptionOrNull()?.let {
            Timber.e("Library indexing work terminated with an exception:", it)
        }
        if (result.isSuccess) {
            RetroAchievementsSyncWork.enqueue(applicationContext)
        }

        return Result.success()
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
