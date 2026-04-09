package com.swordfish.lemuroid.app.utils.games

import android.content.Context
import com.swordfish.lemuroid.app.appextension.isProVersion
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game

class GameUtils {
    companion object {
        fun getGameSubtitle(
            context: Context,
            game: Game,
        ): String {
            val systemName = getSystemNameForGame(context, game)
            val developerName =
                if (game.developer?.isNotBlank() == true) {
                    "- ${game.developer}"
                } else {
                    ""
                }
            return "$systemName $developerName"
        }

        private fun getSystemNameForGame(
            context: Context,
            game: Game,
        ): String {
            val system = GameSystem.findByIdOrNull(game.systemId, isProVersion()) ?: return game.systemId
            return context.getString(system.shortTitleResId)
        }
    }
}
