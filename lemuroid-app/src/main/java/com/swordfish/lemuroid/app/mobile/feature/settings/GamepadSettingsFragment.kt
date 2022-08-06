package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import android.os.Bundle
import android.view.InputDevice
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.input.FlowInputDeviceManager
import com.swordfish.lemuroid.app.shared.settings.GamePadPreferencesHelper
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GamepadSettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var gamePadPreferencesHelper: GamePadPreferencesHelper
    @Inject
    lateinit var inputDeviceManager: FlowInputDeviceManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore =
            SharedPreferencesHelper.getSharedPreferencesDataStore(requireContext())
        setPreferencesFromResource(R.xml.empty_preference_screen, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchOnState(Lifecycle.State.CREATED) {
            inputDeviceManager.getGamePadsObservable()
                .distinctUntilChanged()
                .collect { refreshGamePads(it) }
        }
    }

    private fun refreshGamePads(gamePads: List<InputDevice>) {
        preferenceScreen.removeAll()
        gamePadPreferencesHelper.addGamePadsPreferencesToScreen(
            requireContext(),
            preferenceScreen,
            gamePads
        )
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.pref_key_reset_gamepad_bindings) -> lifecycleScope.launch {
                handleResetBindings()
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private suspend fun handleResetBindings() {
        inputDeviceManager.resetAllBindings()
        refreshGamePads(inputDeviceManager.getGamePadsObservable().first())
    }

    @dagger.Module
    class Module
}
