package com.swordfish.lemuroid.app.tv.settings

import android.content.Context
import android.os.Bundle
import android.view.InputDevice
import android.view.View
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.library.PendingOperationsMonitor
import com.swordfish.lemuroid.app.shared.settings.SaveSyncPreferences
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.common.coroutines.safeCollect
import com.swordfish.lemuroid.common.kotlin.NTuple2
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class TVSettingsFragment : LeanbackPreferenceFragmentCompat() {
    @Inject
    lateinit var settingsInteractor: SettingsInteractor

    @Inject
    lateinit var biosPreferences: BiosPreferences

    @Inject
    lateinit var gamePadPreferencesHelper: GamePadPreferencesHelper

    @Inject
    lateinit var inputDeviceManager: InputDeviceManager

    @Inject
    lateinit var coresSelectionPreferences: CoresSelectionPreferences

    @Inject
    lateinit var saveSyncManager: SaveSyncManager

    lateinit var saveSyncPreferences: SaveSyncPreferences

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)

        saveSyncPreferences = SaveSyncPreferences(saveSyncManager)

        super.onAttach(context)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        launchOnState(Lifecycle.State.CREATED) {
            val gamePadStatus =
                combine(
                    inputDeviceManager.getGamePadsObservable(),
                    inputDeviceManager.getEnabledInputsObservable(),
                    ::NTuple2,
                )

            gamePadStatus
                .distinctUntilChanged()
                .collect { (pads, enabledPads) -> addGamePadBindingsScreen(pads, enabledPads) }
        }

        launchOnState(Lifecycle.State.RESUMED) {
            inputDeviceManager.getEnabledInputsObservable()
                .distinctUntilChanged()
                .collect { refreshGamePadBindingsScreen(it) }
        }

        launchOnState(Lifecycle.State.RESUMED) {
            getSaveSyncScreen()?.let { screen ->
                PendingOperationsMonitor(requireContext())
                    .anySaveOperationInProgress()
                    .safeCollect { syncInProgress ->
                        saveSyncPreferences.updatePreferences(screen, syncInProgress)
                    }
            }
        }
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        preferenceManager.preferenceDataStore =
            SharedPreferencesHelper.getSharedPreferencesDataStore(requireContext())
        setPreferencesFromResource(R.xml.tv_settings, rootKey)

        getCoresSelectionScreen()?.let {
            coresSelectionPreferences.addCoresSelectionPreferences(it)
        }

        getBiosInfoPreferenceScreen()?.let {
            biosPreferences.addBiosPreferences(it)
        }

        getAdvancedSettingsPreferenceScreen()?.let {
            AdvancedSettingsPreferences.updateCachePreferences(it)
        }

        getSaveSyncScreen()?.let {
            if (saveSyncManager.isSupported()) {
                saveSyncPreferences.addSaveSyncPreferences(it)
            }
            it.isVisible = saveSyncManager.isSupported()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSaveSyncScreen()
    }

    private fun getGamePadPreferenceScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_open_gamepad_settings))
    }

    private fun getSaveSyncScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_open_save_sync_settings))
    }

    private fun getCoresSelectionScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_open_cores_selection))
    }

    private fun getBiosInfoPreferenceScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_display_bios_info))
    }

    private fun getAdvancedSettingsPreferenceScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_advanced_settings))
    }

    private fun addGamePadBindingsScreen(
        gamePads: List<InputDevice>,
        enabledGamePads: List<InputDevice>,
    ) {
        lifecycleScope.launch {
            getGamePadPreferenceScreen()?.let {
                it.removeAll()
                gamePadPreferencesHelper.addGamePadsPreferencesToScreen(
                    it.context,
                    it,
                    gamePads,
                    enabledGamePads,
                )
            }
        }
    }

    private fun refreshGamePadBindingsScreen(enabledGamePads: List<InputDevice>) {
        lifecycleScope.launch {
            getGamePadPreferenceScreen()?.let {
                gamePadPreferencesHelper.refreshGamePadsPreferencesToScreen(it, enabledGamePads)
            }
        }
    }

    private fun refreshSaveSyncScreen() {
        getSaveSyncScreen()?.let {
            saveSyncPreferences.updatePreferences(it, false)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (saveSyncPreferences.onPreferenceTreeClick(activity, preference)) {
            return true
        }

        when (preference.key) {
            getString(R.string.pref_key_reset_gamepad_bindings) ->
                lifecycleScope.launch {
                    handleResetGamePadBindings()
                }
            getString(R.string.pref_key_reset_settings) -> handleResetSettings()
        }
        return super.onPreferenceTreeClick(preference)
    }

    private suspend fun handleResetGamePadBindings() {
        inputDeviceManager.resetAllBindings()
        refreshGamePadBindingsScreen(inputDeviceManager.getGamePadsObservable().first())
    }

    private fun handleResetSettings() {
        settingsInteractor.resetAllSettings()
        activity?.finish()
    }

    @dagger.Module
    class Module
}
