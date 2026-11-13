package come.asg.ominoy.app.mobile.feature.gamemenu.coreoptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import come.asg.ominoy.app.shared.input.InputDeviceManager
import kotlinx.coroutines.flow.map

class GameMenuCoreOptionsViewModel(
    val inputDeviceManager: InputDeviceManager,
) : ViewModel() {
    class Factory(
        val inputDeviceManager: InputDeviceManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GameMenuCoreOptionsViewModel(inputDeviceManager) as T
    }

    val connectedGamePads = inputDeviceManager.getGamePadsObservable().map { it.size }
}
