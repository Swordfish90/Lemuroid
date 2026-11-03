package come.asg.lemudroid.app.shared.library

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import come.asg.lemudroid.app.mobile.shared.NotificationsManager
import come.asg.lemudroid.app.utils.android.createSyncForegroundInfo
import come.asg.lemudroid.lib.injection.AndroidWorkerInjection
import come.asg.lemudroid.lib.injection.WorkerKey
import come.asg.lemudroid.lib.library.LemuroidLibrary
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
    lateinit var lemuroidLibrary: LemuroidLibrary

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
                    lemuroidLibrary.indexLibrary()
                }
            }

        result.exceptionOrNull()?.let {
            Timber.e("Library indexing work terminated with an exception:", it)
        }
        if (result.isSuccess) {
            // Dynamic/free builds download cores on demand. Start that work after
            // indexing so the first game launch does not usually pay the download cost.
            LibraryIndexScheduler.scheduleCoreUpdate(applicationContext)
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
