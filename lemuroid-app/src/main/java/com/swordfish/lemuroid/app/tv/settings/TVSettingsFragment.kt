package com.swordfish.lemuroid.app.tv.settings

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsInteractor
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose

class TVSettingsFragment : LeanbackPreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.tv_settings, rootKey)
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
}
