package come.asg.ominoy.app.shared.startup

import android.content.Context
import androidx.startup.Initializer
import come.asg.ominoy.app.shared.game.GameProcessLock
import timber.log.Timber

class GameProcessInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Timber.i("Requested initialization of game process tasks")
        GameProcessLock.acquire(context.applicationContext)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(DebugInitializer::class.java)
}
