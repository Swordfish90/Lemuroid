package com.swordfish.lemuroid.app.shared

import android.content.Context
import com.swordfish.lemuroid.app.feature.game.GameLauncherActivity
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.updateAsync
import com.swordfish.lemuroid.lib.library.db.entity.Game

class GameInteractor(
    private val context: Context,
    private val retrogradeDb: RetrogradeDatabase
) {
    fun onGameClick(game: Game) {
        GameLauncherActivity.launchGame(context, game)
        retrogradeDb.gameDao().updateAsync(game.copy(lastPlayedAt = System.currentTimeMillis())).subscribe()
    }

    fun onFavoriteToggle(game: Game, isFavorite: Boolean) {
        retrogradeDb.gameDao().updateAsync(game.copy(isFavorite = isFavorite)).subscribe()
    }
}
