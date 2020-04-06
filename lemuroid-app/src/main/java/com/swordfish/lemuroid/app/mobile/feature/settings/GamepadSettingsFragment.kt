package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.settings.GamepadBindingsPreferences
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class GamepadSettingsFragment : PreferenceFragmentCompat() {

    @Inject lateinit var gamepadBindingsPreferences: GamepadBindingsPreferences

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.empty_preference_screen, rootKey)

        gamepadBindingsPreferences.getPreferences(requireContext()).forEach { preference ->
            preferenceScreen.addPreference(preference)
        }
    }

    @dagger.Module
    class Module
}
