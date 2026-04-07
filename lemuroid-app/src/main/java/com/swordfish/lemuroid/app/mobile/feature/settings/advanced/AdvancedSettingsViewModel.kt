package com.swordfish.lemuroid.app.mobile.feature.settings.advanced

import android.content.Context
import android.net.Uri
import android.text.format.Formatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.lib.citra.Citra3DSKeysManager
import com.swordfish.lemuroid.lib.storage.cache.CacheCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class AdvancedSettingsViewModel(
    appContext: Context,
    private val settingsInteractor: SettingsInteractor,
    private val citra3DSKeysManager: Citra3DSKeysManager,
) : ViewModel() {
    class Factory(
        private val appContext: Context,
        private val settingsInteractor: SettingsInteractor,
        private val citra3DSKeysManager: Citra3DSKeysManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdvancedSettingsViewModel(appContext, settingsInteractor, citra3DSKeysManager) as T
        }
    }

    data class CacheState(
        val default: String,
        val values: List<String>,
        val displayNames: List<String>,
    )

    data class State(val cache: CacheState)

    data class KeysState(
        val keysPresent: Boolean,
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    val uiState =
        initializeState(appContext)
            .stateIn(viewModelScope, started = SharingStarted.Lazily, null)

    private val _keysState = MutableStateFlow(KeysState(keysPresent = false))
    val keysState: StateFlow<KeysState> = _keysState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _keysState.value = KeysState(keysPresent = citra3DSKeysManager.keysPresent())
        }
    }

    private fun initializeState(appContext: Context): Flow<State?> =
        flow {
            val supportedCacheValues = CacheCleaner.getSupportedCacheLimits()

            val default = CacheCleaner.getDefaultCacheLimit().toString()

            val displayNames =
                supportedCacheValues
                    .map { getSizeLabel(appContext, it) }

            val values =
                supportedCacheValues
                    .map { it.toString() }

            emit(State(CacheState(default, values, displayNames)))
        }

    private fun getSizeLabel(
        appContext: Context,
        size: Long,
    ): String {
        return Formatter.formatShortFileSize(appContext, size)
    }

    fun resetAllSettings() {
        settingsInteractor.resetAllSettings()
    }

    fun installKeysFromUri(
        context: Context,
        uri: Uri,
    ) {
        viewModelScope.launch {
            _keysState.value = _keysState.value.copy(isLoading = true, error = null)
            try {
                citra3DSKeysManager.installFromUri(context, uri)
                _keysState.value = KeysState(keysPresent = true)
            } catch (e: IOException) {
                citra3DSKeysManager.deleteKeys()
                _keysState.value = KeysState(keysPresent = false, error = e.message ?: "Failed to load file")
            }
        }
    }

    fun installKeysFromUrl(url: String) {
        viewModelScope.launch {
            _keysState.value = _keysState.value.copy(isLoading = true, error = null)
            try {
                citra3DSKeysManager.installFromUrl(url)
                _keysState.value = KeysState(keysPresent = true)
            } catch (e: IOException) {
                citra3DSKeysManager.deleteKeys()
                _keysState.value = KeysState(keysPresent = false, error = e.message ?: "Download failed")
            }
        }
    }

    fun deleteKeys() {
        viewModelScope.launch {
            _keysState.value = _keysState.value.copy(isLoading = true, error = null)
            try {
                citra3DSKeysManager.deleteKeys()
                _keysState.value = KeysState(keysPresent = false)
            } catch (e: IOException) {
                _keysState.value = KeysState(keysPresent = true, error = e.message ?: "Failed to delete keys")
            }
        }
    }

    fun clearError() {
        _keysState.value = _keysState.value.copy(error = null)
    }
}
