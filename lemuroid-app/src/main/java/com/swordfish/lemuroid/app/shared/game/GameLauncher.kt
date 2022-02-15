package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import com.swordfish.lemuroid.app.shared.main.GameLaunchTaskHandler
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy

class GameLauncher(
    private val coresSelection: CoresSelection,
    private val gameLaunchTaskHandler: GameLaunchTaskHandler
) {

    fun launchGameAsync(activity: Activity, game: Game, loadSave: Boolean, leanback: Boolean) {
        val system = GameSystem.findById(game.systemId)
        coresSelection.getCoreConfigForSystem(system)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                gameLaunchTaskHandler.handleGameStart(activity.applicationContext)
                BaseGameActivity.launchGame(activity, it, game, loadSave, leanback)
            }
    }
}
