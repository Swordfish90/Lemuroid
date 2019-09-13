package com.codebutler.retrograde.app.feature.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.codebutler.retrograde.R
import com.codebutler.retrograde.lib.library.GameLibrary
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject lateinit var gameLibrary: GameLibrary

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.retrograde_mobile_prefs, rootKey)
    }

    // TODO FILIPPO Replace my_preference with something meaningful.
    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "my_preference" -> gameLibrary.indexGames().subscribe()
        }

        return super.onPreferenceTreeClick(preference)
    }

    @dagger.Module
    class Module
}
