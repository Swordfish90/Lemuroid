package com.swordfish.lemuroid.app.shared

import android.app.Activity
import com.swordfish.lemuroid.app.shared.game.GameLauncherActivity
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.updateAsync
import com.swordfish.lemuroid.lib.library.db.entity.Game
import io.reactivex.Completable
import java.util.concurrent.TimeUnit

class GameInteractor(
    private val activity: Activity,
    private val retrogradeDb: RetrogradeDatabase,
    private val useLeanback: Boolean
) {
    fun onGamePlay(game: Game) {
        GameLauncherActivity.launchGame(activity, game, true, useLeanback)
        updateGamePlayedTimestamp(game)
    }

    fun onGameRestart(game: Game) {
        GameLauncherActivity.launchGame(activity, game, false, useLeanback)
        updateGamePlayedTimestamp(game)
    }

    fun onFavoriteToggle(game: Game, isFavorite: Boolean) {
        retrogradeDb.gameDao().updateAsync(game.copy(isFavorite = isFavorite)).subscribe()
    }

    private fun updateGamePlayedTimestamp(game: Game) {
        Completable.timer(DB_UPDATE_DELAY_SECONDS, TimeUnit.SECONDS)
            .andThen(retrogradeDb.gameDao().updateAsync(game.copy(lastPlayedAt = System.currentTimeMillis())))
            .subscribe()
    }

    companion object {
        private const val DB_UPDATE_DELAY_SECONDS = 1L
    }
}
