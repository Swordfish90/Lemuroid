package com.codebutler.retrograde.app.feature.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.codebutler.retrograde.R
import com.codebutler.retrograde.lib.library.LibraryIndexWork
import dagger.android.support.AndroidSupportInjection

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.retrograde_mobile_prefs, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            getString(R.string.pref_key_rescan) -> handleRescan()
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun handleRescan() {
        WorkManager.getInstance(context!!).enqueueUniqueWork(
                LibraryIndexWork.UNIQUE_WORK_ID,
                ExistingWorkPolicy.APPEND,
                LibraryIndexWork.newRequest()
        )
    }

    @dagger.Module
    class Module
}
