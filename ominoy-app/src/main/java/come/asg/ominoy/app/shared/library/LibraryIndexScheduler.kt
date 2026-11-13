package come.asg.ominoy.app.shared.library

import android.content.Context
import androidx.work.WorkManager

object LibraryIndexScheduler {
    val LIBRARY_INDEX_WORK_ID: String = LibraryIndexWork::class.java.simpleName

    fun scheduleLibrarySync(applicationContext: Context) {
        WorkManager
            .getInstance(applicationContext)
            // Queue a refresh behind an active scan instead of replacing it.
            // This prevents a folder selection/rescan action from cancelling
            // the scan that is already discovering ROMs.
            .beginUniqueWork(
                LIBRARY_INDEX_WORK_ID,
                androidx.work.ExistingWorkPolicy.APPEND_OR_REPLACE,
                androidx.work.OneTimeWorkRequest.from(LibraryIndexWork::class.java),
            ).enqueue()
    }

    fun cancelLibrarySync(applicationContext: Context) {
        WorkManager.getInstance(applicationContext).cancelUniqueWork(LIBRARY_INDEX_WORK_ID)
    }
}
