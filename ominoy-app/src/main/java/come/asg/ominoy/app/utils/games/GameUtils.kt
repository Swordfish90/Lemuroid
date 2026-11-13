package come.asg.ominoy.app.utils.games

import android.content.Context
import come.asg.ominoy.lib.library.GameSystem
import come.asg.ominoy.lib.library.db.entity.Game

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
            val systemTitleResource = GameSystem.findByIdOrNull(game.systemId)?.shortTitleResId
            return systemTitleResource?.let(context::getString) ?: ""
        }
    }
}
