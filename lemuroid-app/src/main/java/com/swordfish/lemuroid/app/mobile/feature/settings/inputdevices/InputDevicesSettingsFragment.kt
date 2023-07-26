package com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.InputDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.swordfish.lemuroid.app.mobile.feature.input.GamePadBindingActivity
import com.swordfish.lemuroid.app.shared.input.InputBindingUpdater
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.RetroKey
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class InputDevicesSettingsFragment : Fragment() {

    @Inject
    lateinit var inputDeviceManager: InputDeviceManager

    private val inputDevicesSettingsViewModel: InputDevicesSettingsViewModel by viewModels {
        val applicationContext = requireContext().applicationContext
        InputDevicesSettingsViewModel.Factory(applicationContext, inputDeviceManager)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val state = inputDevicesSettingsViewModel.uiState
                    .collectAsState(InputDevicesSettingsViewModel.State())

                InputDevicesSettingsScreen(state.value, ::onBindingClicked)
            }
        }
    }

    private fun onBindingClicked(device: InputDevice, retroKey: RetroKey) {
        val intent = Intent(requireContext(), GamePadBindingActivity::class.java).apply {
            putExtra(InputBindingUpdater.REQUEST_DEVICE, device)
            putExtra(InputBindingUpdater.REQUEST_RETRO_KEY, retroKey.keyCode)
        }
        requireContext().startActivity(intent)
    }

    @dagger.Module
    class Module
}
