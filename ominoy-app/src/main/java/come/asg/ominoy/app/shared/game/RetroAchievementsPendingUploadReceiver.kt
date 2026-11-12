package come.asg.ominoy.app.shared.game

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Receives upload requests from the :game process in the default application process. */
class RetroAchievementsPendingUploadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        RetroAchievementsPendingUploadWork.enqueue(context.applicationContext)
    }
}
