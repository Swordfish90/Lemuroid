package com.codebutler.retrograde.storage.webdav

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragment

class WebDavPreferenceFragment : LeanbackPreferenceFragment() {

    companion object {
        const val PREFS_NAME = "webdav"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = PREFS_NAME
        addPreferencesFromResource(R.xml.webdav_prefs)
    }
}
