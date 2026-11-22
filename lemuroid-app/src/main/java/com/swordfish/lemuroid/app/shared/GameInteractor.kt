package com.swordfish.lemuroid.app.shared

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.shortcuts.ShortcutsGenerator
import com.swordfish.lemuroid.app.shared.game.GameLauncher
import com.swordfish.lemuroid.app.shared.main.BusyActivity
import com.swordfish.lemuroid.common.displayToast
import com.swordfish.lemuroid.lib.library.covers.CustomCoverManager
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.scraper.MediaStorageManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    fun onSetCustomCoverUri(game: Game, sourceUri: String) {
        GlobalScope.launch {
            val localUri = customCoverManager.importCover(game.id, Uri.parse(sourceUri))
            retrogradeDb.gameDao().update(game.copy(customCoverUri = localUri))
        }
    }


    fun onClearCustomCoverUri(game: Game) {
        GlobalScope.launch {
            customCoverManager.deleteCover(game.id)
            retrogradeDb.gameDao().update(game.copy(customCoverUri = null))
        }
    }


    fun onDeleteGame(game: Game) {
        val appContext = activity.activity().applicationContext
        GlobalScope.launch(Dispatchers.IO) {
            val romBasename = game.fileName.substringBeforeLast('.')

            val dataFiles = runCatching {
                retrogradeDb.dataFileDao().selectDataFilesForGame(game.id)
            }.getOrDefault(emptyList())
            dataFiles.forEach { dataFile ->
                deleteUriFile(appContext, dataFile.fileUri)
            }
            if (dataFiles.isNotEmpty()) {
                runCatching { retrogradeDb.dataFileDao().delete(dataFiles) }
            }

            val romDeleted = deleteUriFile(appContext, game.fileUri)

            runCatching {
                MediaStorageManager(appContext).deleteMediaForGame(game.systemId, romBasename)
            }
            runCatching {
                customCoverManager.deleteCover(game.id)
            }

            val directoriesManager = DirectoriesManager(appContext)
            runCatching {
                File(directoriesManager.getSavesDirectory(), "$romBasename.srm").delete()
            }
            val stateDirs = listOf(
                directoriesManager.getStatesDirectory(),
                @Suppress("DEPRECATION")
                directoriesManager.getInternalStatesDirectory(),
                directoriesManager.getStatesPreviewDirectory(),
            )
            stateDirs.forEach { rootDir ->
                runCatching {
                    deleteMatchingFilesRecursively(rootDir, game.fileName, romBasename)
                }
            }

            runCatching { retrogradeDb.gameDetailsDao().deleteDetailsForGame(game.id) }
            runCatching { retrogradeDb.cheatDao().deleteCheatsForGame(game.id) }
            runCatching { retrogradeDb.gameDao().delete(listOf(game)) }

            withContext(Dispatchers.Main) {
                val message = if (romDeleted) {
                    appContext.getString(R.string.toast_delete_rom_success, game.displayName())
                } else {
                    appContext.getString(R.string.toast_delete_rom_partial, game.displayName())
                }
                Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteUriFile(context: Context, uriString: String): Boolean {
        val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return false
        return when (uri.scheme) {
            "content" -> {
                val deletedByContract = runCatching {
                    DocumentsContract.deleteDocument(context.contentResolver, uri)
                }.getOrDefault(false)
                if (deletedByContract) {
                    true
                } else {
                    runCatching {
                        DocumentFile.fromSingleUri(context, uri)?.delete() == true
                    }.getOrDefault(false)
                }
            }
            "file", null -> {
                val path = uri.path ?: return false
                val file = File(path)
                !file.exists() || file.delete()
            }
            else -> false
        }
    }

    private fun deleteMatchingFilesRecursively(dir: File, fileName: String, romBasename: String) {
        if (!dir.exists()) return
        dir.listFiles()?.forEach { child ->
            if (child.isDirectory) {
                deleteMatchingFilesRecursively(child, fileName, romBasename)
            } else if (child.name.startsWith("$fileName.") || child.name.startsWith("$romBasename.")) {
                child.delete()
            }
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
