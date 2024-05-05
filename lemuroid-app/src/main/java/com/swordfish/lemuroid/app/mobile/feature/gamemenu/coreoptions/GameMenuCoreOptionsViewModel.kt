package com.swordfish.lemuroid.app.mobile.feature.gamemenu.coreoptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import kotlinx.coroutines.flow.map

class GameMenuCoreOptionsViewModel(val inputDeviceManager: InputDeviceManager) : ViewModel() {
    class Factory(val inputDeviceManager: InputDeviceManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameMenuCoreOptionsViewModel(inputDeviceManager) as T
        }
    }

    val connectedGamePads = inputDeviceManager.getGamePadsObservable().map { it.size }
}
