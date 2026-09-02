package com.swordfish.lemuroid.app.shared

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.shortcuts.ShortcutsGenerator
import com.swordfish.lemuroid.app.shared.covers.CustomCoverManager
import com.swordfish.lemuroid.app.shared.game.GameLauncher
import com.swordfish.lemuroid.app.shared.main.BusyActivity
import com.swordfish.lemuroid.common.displayToast
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameInteractor(
    private val activity: BusyActivity,
    private val retrogradeDb: RetrogradeDatabase,
    private val useLeanback: Boolean,
    private val shortcutsGenerator: ShortcutsGenerator,
    private val gameLauncher: GameLauncher,
) {
    fun onGamePlay(game: Game) {
        if (!ensureNotBusy()) {
            return
        }
        if (!ensureNotificationsPermissionAvailable()) {
            return
        }
        gameLauncher.launchGameAsync(activity.activity(), game, true, useLeanback)
    }

    fun onGameRestart(game: Game) {
        if (!ensureNotBusy()) {
            return
        }
        if (!ensureNotificationsPermissionAvailable()) {
            return
        }
        gameLauncher.launchGameAsync(activity.activity(), game, false, useLeanback)
    }

    fun onFavoriteToggle(
        game: Game,
        isFavorite: Boolean,
    ) {
        GlobalScope.launch {
            retrogradeDb.gameDao().update(game.copy(isFavorite = isFavorite))
        }
    }

    fun onSetCustomThumbnail(game: Game, uri: Uri) {
        GlobalScope.launch(Dispatchers.IO) {
            val context = activity.activity().applicationContext
            val path = CustomCoverManager.save(context, game.id, uri)
            if (path == null) {
                withContext(Dispatchers.Main) {
                    activity.activity().displayToast(R.string.game_custom_thumbnail_error)
                }
                return@launch
            }
            if (game.customCoverPath != null && game.customCoverPath != path) {
                CustomCoverManager.delete(game.customCoverPath)
            }
            retrogradeDb.gameDao().update(game.copy(customCoverPath = path))
        }
    }

    fun onRemoveCustomThumbnail(game: Game) {
        GlobalScope.launch(Dispatchers.IO) {
            CustomCoverManager.delete(game.customCoverPath)
            retrogradeDb.gameDao().update(game.copy(customCoverPath = null))
        }
    }

    fun onRenameGame(game: Game, displayName: String?) {
        GlobalScope.launch(Dispatchers.IO) {
            val normalizedName = displayName?.trim()?.takeIf { it.isNotEmpty() }
            retrogradeDb.gameDao().update(game.copy(customDisplayName = normalizedName))
        }
    }

    fun onCreateShortcut(game: Game) {
        GlobalScope.launch {
            shortcutsGenerator.pinShortcutForGame(game)
        }
    }

    fun supportShortcuts(): Boolean {
        return shortcutsGenerator.supportShortcuts()
    }

    private fun ensureNotificationsPermissionAvailable(): Boolean {
        if (useLeanback || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        val permissionResult =
            ContextCompat.checkSelfPermission(
                activity.activity(),
                Manifest.permission.POST_NOTIFICATIONS,
            )

        if (permissionResult == PackageManager.PERMISSION_GRANTED) {
            return true
        }

        activity.activity().displayToast(R.string.game_interactor_notification_permission_required)
        return false
    }

    private fun ensureNotBusy(): Boolean {
        if (activity.isBusy()) {
            activity.activity().displayToast(R.string.game_interactory_busy)
            return false
        }
        return true
    }
}
