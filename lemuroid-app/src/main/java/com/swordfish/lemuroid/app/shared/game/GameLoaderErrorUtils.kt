package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.displayErrorDialog
import com.swordfish.lemuroid.lib.game.GameLoaderError
import com.swordfish.lemuroid.lib.library.SystemCoreConfig

fun Activity.displayGameLoaderError(gameError: GameLoaderError, coreConfig: SystemCoreConfig) {

    val messageId = when (gameError) {
        GameLoaderError.GL_INCOMPATIBLE -> getString(R.string.game_loader_error_gl_incompatible)
        GameLoaderError.GENERIC -> getString(R.string.game_loader_error_generic)
        GameLoaderError.LOAD_CORE -> getString(R.string.game_loader_error_load_core)
        GameLoaderError.LOAD_GAME -> getString(R.string.game_loader_error_load_game)
        GameLoaderError.SAVES -> getString(R.string.game_loader_error_save)
        GameLoaderError.MISSING_BIOS -> getString(
            R.string.game_loader_error_missing_bios,
            coreConfig.requiredBIOSFiles.joinToString(",")
        )
    }

    this.displayErrorDialog(messageId, getString(R.string.ok)) { finish() }
}
