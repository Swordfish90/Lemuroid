package com.swordfish.lemuroid.app.shared

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.isProVersion
import com.swordfish.lemuroid.app.mobile.feature.shortcuts.ShortcutsGenerator
import com.swordfish.lemuroid.app.shared.game.GameLauncher
import com.swordfish.lemuroid.app.shared.main.BusyActivity
import com.swordfish.lemuroid.common.displayToast
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.GameSystemHelperImpl
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GameInteractor(
    private val activity: BusyActivity,
    private val retrogradeDb: RetrogradeDatabase,
    private val useLeanback: Boolean,
    private val shortcutsGenerator: ShortcutsGenerator,
    private val gameLauncher: GameLauncher,
    private val gameSystemHelper: GameSystemHelperImpl,
) {
    fun onGamePlay(game: Game) {
        if (!ensureNotBusy()) {
            return
        }
        if (!ensureNotificationsPermissionAvailable()) {
            return
        }
        if (!ensureProFeaturesAvailable(game)) {
            return
        }
        gameLauncher.launchGameAsync(activity.activity(), game, true, useLeanback,gameSystemHelper)
    }

    fun onGameRestart(game: Game) {
        if (!ensureNotBusy()) {
            return
        }
        if (!ensureNotificationsPermissionAvailable()) {
            return
        }
        if (!ensureProFeaturesAvailable(game)) {
            return
        }
        gameLauncher.launchGameAsync(activity.activity(), game, false, useLeanback,gameSystemHelper)
    }

    private fun ensureProFeaturesAvailable(game: Game): Boolean {
        if (isProVersion()) {
            return true
        }

        // Check if game is from 7z archive (pro-only feature)
        val is7z = game.fileUri.endsWith(".7z", ignoreCase = true)
        if (is7z) {
            activity.showProUpgradeFor7z()
            return false
        }

        // Check if game's system is pro-only
        val isProOnlySystem = GameSystem.isSystemProOnly(game.systemId)
        if (isProOnlySystem) {
            activity.showProUpgradeFor7z()
            return false
        }

        return true
    }

    fun onFavoriteToggle(
        game: Game,
        isFavorite: Boolean,
    ) {
        GlobalScope.launch {
            retrogradeDb.gameDao().update(game.copy(isFavorite = isFavorite))
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
