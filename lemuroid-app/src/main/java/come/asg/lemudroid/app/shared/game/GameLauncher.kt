package come.asg.lemudroid.app.shared.game

import android.app.Activity
import come.asg.lemudroid.R
import come.asg.lemudroid.app.shared.main.GameLaunchTaskHandler
import come.asg.lemudroid.common.displayToast
import come.asg.lemudroid.lib.core.CoresSelection
import come.asg.lemudroid.lib.library.GameSystem
import come.asg.lemudroid.lib.library.db.entity.Game
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GameLauncher(
    private val coresSelection: CoresSelection,
    private val gameLaunchTaskHandler: GameLaunchTaskHandler,
) {
    @OptIn(DelicateCoroutinesApi::class)
    fun launchGameAsync(
        activity: Activity,
        game: Game,
        loadSave: Boolean,
        leanback: Boolean,
    ): Boolean {
        if (GameProcessLock.isHeldByAnotherProcess(activity.applicationContext)) {
            activity.displayToast(R.string.game_process_another_game_running)
            return false
        }

        GlobalScope.launch {
            val system = GameSystem.findByIdOrNull(game.systemId)
            if (system == null) {
                activity.displayToast(R.string.game_loader_error_generic)
                return@launch
            }
            val coreConfig = coresSelection.getCoreConfigForSystem(system)
            gameLaunchTaskHandler.handleGameStart(activity.applicationContext)
            BaseGameActivity.launchGame(activity, coreConfig, game, loadSave, leanback)
        }

        return true
    }
}
