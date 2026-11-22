package com.swordfish.lemuroid.app.mobile.feature.settings.general

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.shared.library.PendingOperationsMonitor
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.app.shared.settings.StorageFrameworkPickerLauncher
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val context: Context,
    private val settingsInteractor: SettingsInteractor,
    saveSyncManager: SaveSyncManager,
) : ViewModel() {
    class Factory(
        private val context: Context,
        private val settingsInteractor: SettingsInteractor,
        private val saveSyncManager: SaveSyncManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                context,
                settingsInteractor,
                saveSyncManager,
            ) as T
        }
    }

    data class State(
        val currentDirectories: Set<String> = emptySet(),
        val isSaveSyncSupported: Boolean = false,
    )

    val indexingInProgress = PendingOperationsMonitor(context).anyLibraryOperationInProgress()

    val directoryScanInProgress = PendingOperationsMonitor(context).isDirectoryScanInProgress()

    private fun observeFolders(): Flow<Set<String>> = callbackFlow {
        val prefs = SharedPreferencesHelper.getLegacySharedPreferences(context)
        val foldersKey = context.getString(com.swordfish.lemuroid.lib.R.string.pref_key_external_folders)
        val singleKey = context.getString(com.swordfish.lemuroid.lib.R.string.pref_key_extenral_folder)

        fun currentFolders(): Set<String> {
            val folders = prefs.getStringSet(foldersKey, emptySet()) ?: emptySet()
            if (folders.isEmpty()) {
                val old = prefs.getString(singleKey, null)
                return if (!old.isNullOrBlank()) setOf(old) else emptySet()
            }
            return folders
        }

        trySend(currentFolders())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == foldersKey || key == singleKey) {
                trySend(currentFolders())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val uiState =
        observeFolders()
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
            .map { State(it, saveSyncManager.isSupported()) }

    fun changeLocalStorageFolder() {
        settingsInteractor.changeLocalStorageFolder()
    }

    fun removeFolder(folderUri: String) {
        StorageFrameworkPickerLauncher.removeFolder(context, folderUri)
    }
}
