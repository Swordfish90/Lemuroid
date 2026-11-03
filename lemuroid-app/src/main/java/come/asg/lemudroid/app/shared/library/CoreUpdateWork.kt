package come.asg.lemudroid.app.shared.library

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import come.asg.lemudroid.app.mobile.shared.NotificationsManager
import come.asg.lemudroid.app.utils.android.createSyncForegroundInfo
import come.asg.lemudroid.lib.core.CoreUpdater
import come.asg.lemudroid.lib.core.CoresSelection
import come.asg.lemudroid.lib.injection.AndroidWorkerInjection
import come.asg.lemudroid.lib.injection.WorkerKey
import come.asg.lemudroid.lib.library.GameSystem
import come.asg.lemudroid.lib.library.db.RetrogradeDatabase
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import timber.log.Timber
import java.util.concurrent.CancellationException
import javax.inject.Inject

class CoreUpdateWork(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    @Inject
    lateinit var retrogradeDatabase: RetrogradeDatabase

    @Inject
    lateinit var coreUpdater: CoreUpdater

    @Inject
    lateinit var coresSelection: CoresSelection

    override suspend fun doWork(): Result {
        AndroidWorkerInjection.inject(this)

        Timber.i("Starting core update/install work")

        val notificationsManager = NotificationsManager(applicationContext)

        val foregroundInfo =
            createSyncForegroundInfo(
                NotificationsManager.CORE_INSTALL_NOTIFICATION_ID,
                notificationsManager.installingCoresNotification(),
            )

        setForegroundAsync(foregroundInfo)

        try {
            val cores =
                retrogradeDatabase.gameDao().selectSystems()
                    .asFlow()
                    .mapNotNull { GameSystem.findByIdOrNull(it) }
                    .map { coresSelection.getCoreConfigForSystem(it) }
                    .map { it.coreID }
                    .toList()

            coreUpdater.downloadCores(applicationContext, cores)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Core update work failed with exception: ${e.message}")
            return if (runAttemptCount < MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
        }

        return Result.success()
    }

    private companion object {
        const val MAX_RETRY_ATTEMPTS = 5
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
