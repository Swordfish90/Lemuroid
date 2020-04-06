package com.swordfish.lemuroid.app.tv.settings

import android.content.Context
import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.settings.GamepadBindingsPreferences
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TVSettingsFragment : LeanbackPreferenceFragmentCompat() {

    @Inject lateinit var gamepadBindingsPreferences: GamepadBindingsPreferences

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.tv_settings, rootKey)

        val gamepadPreferenceScreen = findPreference<PreferenceScreen>(resources.getString(R.string.pref_key_open_gamepad_bindings))
        gamepadBindingsPreferences.getPreferences(requireContext()).forEach {
            gamepadPreferenceScreen?.addPreference(it)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            getString(R.string.pref_key_clear_cores_cache) -> handleClearCacheCores()
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun handleClearCacheCores() {
        SettingsInteractor(requireContext())
            .clearCoresCache()
            .doAfterTerminate { activity?.finish() }
            .autoDispose(scope())
            .subscribe()
    }

    @dagger.Module
    class Module
}
