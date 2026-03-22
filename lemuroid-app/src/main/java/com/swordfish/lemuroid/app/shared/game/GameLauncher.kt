package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.main.GameLaunchTaskHandler
import com.swordfish.lemuroid.common.displayToast
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
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
            val system = GameSystem.findById(game.systemId)
            val coreConfig = coresSelection.getCoreConfigForSystem(system)
            
            // Perform an immediate bidirectional local sync right before the Core launches.
            // This ensures any newly placed manually copied .sav files are injected into Lemuroid!
            com.swordfish.lemuroid.app.shared.savesync.LocalSaveSyncWork.performSync(activity.applicationContext)

            gameLaunchTaskHandler.handleGameStart(activity.applicationContext)
            BaseGameActivity.launchGame(activity, coreConfig, game, loadSave, leanback)
        }

        return true
    }
}
