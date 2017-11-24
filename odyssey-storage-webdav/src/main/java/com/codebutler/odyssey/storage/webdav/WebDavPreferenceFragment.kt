package com.codebutler.odyssey.storage.webdav

import android.os.Bundle
import android.support.v17.preference.LeanbackPreferenceFragment

class WebDavPreferenceFragment : LeanbackPreferenceFragment() {

    companion object {
        const val PREFS_NAME = "webdav"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = PREFS_NAME
        addPreferencesFromResource(R.xml.webdav_prefs)
    }
}
