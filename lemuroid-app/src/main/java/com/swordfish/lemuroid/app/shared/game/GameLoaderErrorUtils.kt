package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.displayErrorDialog
import com.swordfish.lemuroid.lib.game.GameLoaderError

fun Activity.displayGameLoaderError(gameError: GameLoaderError) {
    val messageId = when (gameError) {
        GameLoaderError.GL_INCOMPATIBLE -> (R.string.game_loader_error_gl_incompatible)
        GameLoaderError.GENERIC -> R.string.game_loader_error_generic
        GameLoaderError.LOAD_CORE -> R.string.game_loader_error_load_core
        GameLoaderError.LOAD_GAME -> R.string.game_loader_error_load_game
        GameLoaderError.SAVES -> R.string.game_loader_error_save
    }

    this.displayErrorDialog(messageId, R.string.ok) { finish() }
}
