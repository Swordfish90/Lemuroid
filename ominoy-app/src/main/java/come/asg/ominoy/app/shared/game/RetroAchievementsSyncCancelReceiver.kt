package come.asg.ominoy.app.shared.game

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RetroAchievementsSyncCancelReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        RetroAchievementsSyncWork.cancel(context.applicationContext)
    }
}
