package com.swordfish.lemuroid.app.mobile.feature.settings.general

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.library.PendingOperationsMonitor
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

import android.content.SharedPreferences
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper

class SettingsViewModel(
    context: Context,
    private val settingsInteractor: SettingsInteractor,
    saveSyncManager: SaveSyncManager,
    sharedPreferences: SharedPreferences,
) : ViewModel() {
    private val flowSharedPreferences = FlowSharedPreferences(sharedPreferences)
    class Factory(
        private val context: Context,
        private val settingsInteractor: SettingsInteractor,
        private val saveSyncManager: SaveSyncManager,
        private val sharedPreferences: SharedPreferences,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                context,
                settingsInteractor,
                saveSyncManager,
                sharedPreferences,
            ) as T
        }
    }

    data class State(
        val currentDirectory: String = "",
        val saveDirectoryUri: String? = null,
        val isSaveSyncSupported: Boolean = false,
    )

    val indexingInProgress = PendingOperationsMonitor(context).anyLibraryOperationInProgress()

    val directoryScanInProgress = PendingOperationsMonitor(context).isDirectoryScanInProgress()

    val uiState =
        flowSharedPreferences.getString(context.getString(com.swordfish.lemuroid.lib.R.string.pref_key_extenral_folder))
            .asFlow()
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.Lazily, "")
            .map {
                val saveDirectoryUri = flowSharedPreferences.getString("pref_key_save_storage_folder", "").get()
                State(
                    currentDirectory = it,
                    saveDirectoryUri = if (saveDirectoryUri.isEmpty()) null else saveDirectoryUri,
                    isSaveSyncSupported = saveSyncManager.isSupported()
                )
            }

    fun changeLocalStorageFolder() {
        settingsInteractor.changeLocalStorageFolder()
    }
}
