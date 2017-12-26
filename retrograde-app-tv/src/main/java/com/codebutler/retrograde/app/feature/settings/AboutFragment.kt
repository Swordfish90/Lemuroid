package com.codebutler.retrograde.app.feature.settings

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v14.preference.PreferenceFragment
import android.support.v17.preference.LeanbackPreferenceFragment
import android.support.v17.preference.LeanbackSettingsFragment
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceScreen
import com.codebutler.retrograde.BuildConfig
import com.codebutler.retrograde.R

class AboutFragment : LeanbackSettingsFragment() {

    override fun onPreferenceStartInitialScreen() {
        startPreferenceFragment(AboutPrefFragment())
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragment, pref: PreferenceScreen): Boolean = false

    override fun onPreferenceStartFragment(caller: PreferenceFragment, pref: Preference): Boolean {
        val fragment = Class.forName(pref.fragment).newInstance() as Fragment
        startPreferenceFragment(fragment)
        return true
    }

    class AboutPrefFragment : LeanbackPreferenceFragment() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.about)

            val versionPref = findPreference(getString(R.string.pref_key_version))
            versionPref.summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean =
                when (preference.key) {
                    getString(R.string.pref_key_licenses) -> {
                        startActivity(Intent(activity, LicensesActivity::class.java))
                        true
                    }
                    else -> super.onPreferenceTreeClick(preference)
                }
    }
}
