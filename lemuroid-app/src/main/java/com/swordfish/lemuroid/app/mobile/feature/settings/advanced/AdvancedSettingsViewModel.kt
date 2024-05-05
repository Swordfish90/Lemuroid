package com.swordfish.lemuroid.app.mobile.feature.settings.advanced

import android.content.Context
import android.text.format.Formatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.lib.storage.cache.CacheCleaner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class AdvancedSettingsViewModel(
    appContext: Context,
    private val settingsInteractor: SettingsInteractor,
) : ViewModel() {
    class Factory(
        private val appContext: Context,
        private val settingsInteractor: SettingsInteractor,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdvancedSettingsViewModel(appContext, settingsInteractor) as T
        }
    }

    data class CacheState(
        val default: String,
        val values: List<String>,
        val displayNames: List<String>,
    )

    data class State(val cache: CacheState)

    val uiState =
        initializeState(appContext)
            .stateIn(viewModelScope, started = SharingStarted.Lazily, null)

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
}
