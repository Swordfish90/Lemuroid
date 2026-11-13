package come.asg.ominoy.app.mobile.feature.settings.inputdevices

import android.content.Context
import android.view.InputDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import come.asg.ominoy.app.shared.input.InputDeviceManager
import come.asg.ominoy.app.shared.input.InputKey
import come.asg.ominoy.app.shared.input.RetroKey
import come.asg.ominoy.app.shared.input.ominoydevice.getOminoyInputDevice
import come.asg.ominoy.app.shared.settings.GameShortcut
import come.asg.ominoy.common.kotlin.reverseLookup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InputDevicesSettingsViewModel(
    appContext: Context,
    private val inputDeviceManager: InputDeviceManager,
) : ViewModel() {
    class Factory(
        val appContext: Context,
        val inputDeviceManager: InputDeviceManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InputDevicesSettingsViewModel(appContext, inputDeviceManager) as T
    }

    data class DeviceView(
        val name: String,
        val key: String,
        val enabledByDefault: Boolean,
    )

    data class BindingsView(
        val keys: Map<RetroKey, InputKey> = emptyMap(),
        val shortcuts: List<GameShortcut> = emptyList(),
    )

    data class State(
        val devices: List<DeviceView> = emptyList(),
        val bindings: Map<InputDevice, BindingsView> = emptyMap(),
    )

    val uiState =
        initializeState(appContext)
            .stateIn(viewModelScope, started = SharingStarted.Lazily, State())

    fun resetAllBindings() {
        viewModelScope.launch {
            inputDeviceManager.resetAllBindings()
        }
    }

    private fun initializeState(context: Context): Flow<State> {
        val devicesViews = getEnabledDevicesViews(context)
        val bindingsViews = getDevicesBindingViews()
        return combine(devicesViews, bindingsViews, ::State)
    }

    private fun getEnabledDevicesViews(context: Context): Flow<List<DeviceView>> =
        inputDeviceManager
            .getDistinctGamePadsObservable()
            .map { devices -> devices.map { buildDeviceView(context, it) } }

    private fun buildDeviceView(
        context: Context,
        device: InputDevice,
    ): DeviceView =
        DeviceView(
            device.name,
            InputDeviceManager.computeEnabledGamePadPreference(device),
            device.getOminoyInputDevice().isEnabledByDefault(context),
        )

    private fun getDevicesBindingViews(): Flow<Map<InputDevice, BindingsView>> {
        val devicesFlow = inputDeviceManager.getEnabledInputsObservable()
        val bindingsFlow = inputDeviceManager.getInputBindingsObservable()
        val shortcutsFlow = inputDeviceManager.getGameShortcutsObservable()

        return combine(devicesFlow, bindingsFlow, shortcutsFlow) { devices, allBindings, allShortcuts ->
            devices.associateWith { device ->
                val shortcuts =
                    allShortcuts[device]?.filter {
                        it.type in device.getOminoyInputDevice().getSupportedShortcuts()
                    } ?: emptyList()
                val keys = allBindings(device).reverseLookup()

                BindingsView(keys, shortcuts)
            }
        }
    }
}
