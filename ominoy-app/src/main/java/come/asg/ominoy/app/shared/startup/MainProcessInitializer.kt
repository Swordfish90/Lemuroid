package come.asg.ominoy.app.shared.startup

import android.content.Context
import androidx.startup.Initializer
import androidx.work.WorkManagerInitializer
import come.asg.ominoy.app.shared.game.RetroAchievementsPendingUploadWork
import come.asg.ominoy.app.shared.library.LibraryIndexScheduler
import come.asg.ominoy.app.shared.savesync.SaveSyncWork
import come.asg.ominoy.lib.preferences.SharedPreferencesHelper
import timber.log.Timber

class MainProcessInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Timber.i("Requested initialization of main process tasks")
        SaveSyncWork.enqueueAutoWork(context, 0)
        RetroAchievementsPendingUploadWork.enqueue(context)
        RetroAchievementsPendingUploadWork.enqueuePeriodic(context)
        val preferences = SharedPreferencesHelper.getLegacySharedPreferences(context)
        val romFolderKey = context.getString(come.asg.ominoy.lib.R.string.pref_key_extenral_folder)
        // Install the only supported core at first startup, before a ROM folder is
        // selected. Library indexing still waits for the user's folder choice.
        if (!preferences.getString(romFolderKey, null).isNullOrBlank()) {
            LibraryIndexScheduler.scheduleLibrarySync(context.applicationContext)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(WorkManagerInitializer::class.java, DebugInitializer::class.java)
}
