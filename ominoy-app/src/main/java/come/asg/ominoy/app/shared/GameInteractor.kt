package come.asg.ominoy.app.shared

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import come.asg.ominoy.R
import come.asg.ominoy.app.mobile.feature.shortcuts.ShortcutsGenerator
import come.asg.ominoy.app.shared.game.GameLauncher
import come.asg.ominoy.app.shared.main.BusyActivity
import come.asg.ominoy.common.displayToast
import come.asg.ominoy.lib.library.covers.CustomCoverManager
import come.asg.ominoy.lib.library.db.RetrogradeDatabase
import come.asg.ominoy.lib.library.db.entity.Game
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class GameInteractor(
    private val activity: BusyActivity,
    private val retrogradeDb: RetrogradeDatabase,
    private val useLeanback: Boolean,
    private val shortcutsGenerator: ShortcutsGenerator,
    private val gameLauncher: GameLauncher,
    private val customCoverManager: CustomCoverManager,
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

    fun onCreateShortcut(game: Game) {
        GlobalScope.launch {
            shortcutsGenerator.pinShortcutForGame(game)
        }
    }

    fun supportShortcuts(): Boolean {
        return shortcutsGenerator.supportShortcuts()
    }

    fun onSetCustomName(game: Game, customName: String) {
        GlobalScope.launch {
            retrogradeDb.gameDao().update(game.copy(customName = customName))
        }
    }

    fun onClearCustomName(game: Game) {
        GlobalScope.launch {
            retrogradeDb.gameDao().update(game.copy(customName = null))
        }
    }

    /**
     * Copies the image at [sourceUri] into app-private storage, then stores
     * the local file URI in the database.  This ensures the cover survives
     * app restarts regardless of where the user picked the file from.
     */
    fun onSetCustomCoverUri(game: Game, sourceUri: String) {
        val hostActivity = activity.activity()
        val owner = hostActivity as? LifecycleOwner
        val scope = owner?.lifecycleScope ?: GlobalScope
        scope.launch {
            try {
                val localUri =
                    withContext(Dispatchers.IO) {
                        customCoverManager.importCover(game.id, Uri.parse(sourceUri))
                    }
                retrogradeDb.gameDao().update(game.copy(customCoverUri = localUri))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to set custom artwork for game ${game.id}")
                withContext(Dispatchers.Main.immediate) {
                    if (!hostActivity.isFinishing && !hostActivity.isDestroyed) {
                        hostActivity.displayToast(R.string.custom_artwork_import_failed)
                    }
                }
            }
        }
    }

    /**
     * Removes the custom cover: deletes the file from private storage and
     * clears the database field.
     */
    fun onClearCustomCoverUri(game: Game) {
        GlobalScope.launch {
            customCoverManager.deleteCover(game.id)
            retrogradeDb.gameDao().update(game.copy(customCoverUri = null))
        }
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
