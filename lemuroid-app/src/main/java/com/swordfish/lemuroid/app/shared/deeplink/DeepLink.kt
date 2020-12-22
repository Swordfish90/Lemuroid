package com.swordfish.lemuroid.app.shared.deeplink

import android.content.Intent
import android.net.Uri
import com.swordfish.lemuroid.lib.library.db.entity.Game

object DeepLink {
    private const val PLAY_GAME_BASE_URI = "lemuroid://play.game"

    private fun uriForGame(game: Game): Uri {
        return Uri.parse("$PLAY_GAME_BASE_URI/${game.id}")
    }

    fun launchIntentForGame(game: Game) = Intent(Intent.ACTION_VIEW, uriForGame(game))
}
