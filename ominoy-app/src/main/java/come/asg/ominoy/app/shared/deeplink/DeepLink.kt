package come.asg.ominoy.app.shared.deeplink

import android.content.Context
import android.content.Intent
import android.net.Uri
import come.asg.ominoy.lib.library.db.entity.Game

object DeepLink {
    fun openLeanbackUri(appContext: Context): Uri {
        return Uri.parse("ominoy://${appContext.packageName}/open-leanback")
    }

    private fun uriForGame(
        appContext: Context,
        game: Game,
    ): Uri {
        return Uri.parse("ominoy://${appContext.packageName}/play-game/id/${game.id}")
    }

    fun launchIntentForGame(
        appContext: Context,
        game: Game,
    ) = Intent(Intent.ACTION_VIEW, uriForGame(appContext, game))
}
