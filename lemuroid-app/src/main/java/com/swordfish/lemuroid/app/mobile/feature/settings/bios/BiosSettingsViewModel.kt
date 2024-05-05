package com.swordfish.lemuroid.app.mobile.feature.settings.bios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.lib.bios.BiosManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class BiosSettingsViewModel(private val biosManager: BiosManager) : ViewModel() {
    class Factory
        @Inject
        constructor(private val biosManager: BiosManager) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BiosSettingsViewModel(biosManager) as T
            }
        }

    val uiState =
        flow { emit(biosManager.getBiosInfoAsync()) }
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                BiosManager.BiosInfo(emptyList(), emptyList()),
            )
}
