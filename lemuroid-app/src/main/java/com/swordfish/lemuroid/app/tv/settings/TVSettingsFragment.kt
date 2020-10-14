package com.swordfish.lemuroid.app.tv.settings

import android.content.Context
import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.settings.BiosPreferences
import com.swordfish.lemuroid.app.shared.settings.GamePadSettingsPreferences
import com.swordfish.lemuroid.app.shared.settings.GamePadManager
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class TVSettingsFragment : LeanbackPreferenceFragmentCompat() {

    @Inject lateinit var settingsInteractor: SettingsInteractor
    @Inject lateinit var biosPreferences: BiosPreferences
    @Inject lateinit var gamePadSettingsPreferences: GamePadSettingsPreferences
    @Inject lateinit var gamePadManager: GamePadManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.tv_settings, rootKey)

        getBiosInfoPreferenceScreen()?.let {
            biosPreferences.addBiosPreferences(it)
        }
    }

    override fun onResume() {
        super.onResume()
        gamePadManager.getGamePadsObservable()
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { refreshGamePadBindingsScreen() }
    }

    private fun getGamePadPreferenceScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_open_gamepad_bindings))
    }

    private fun getBiosInfoPreferenceScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_display_bios_info))
    }

    private fun refreshGamePadBindingsScreen() {
        getGamePadPreferenceScreen()?.let {
            it.removeAll()
            gamePadSettingsPreferences.addGamePadsPreferencesToScreen(requireContext(), it)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            getString(R.string.pref_key_reset_gamepad_bindings) -> handleResetGamePadBindings()
            getString(R.string.pref_key_reset_settings) -> handleResetSettings()
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun handleResetGamePadBindings() {
        gamePadSettingsPreferences.resetAllBindings()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { refreshGamePadBindingsScreen() }
    }

    private fun handleResetSettings() {
        settingsInteractor.resetAllSettings()
        activity?.finish()
    }

    @dagger.Module
    class Module
}
