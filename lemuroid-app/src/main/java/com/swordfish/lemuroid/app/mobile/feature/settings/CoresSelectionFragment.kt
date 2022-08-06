package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.settings.CoresSelectionPreferences
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class CoresSelectionFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var coresSelectionPreferences: CoresSelectionPreferences

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore =
            SharedPreferencesHelper.getSharedPreferencesDataStore(requireContext())
        setPreferencesFromResource(R.xml.empty_preference_screen, rootKey)
        coresSelectionPreferences.addCoresSelectionPreferences(preferenceScreen)
    }

    @dagger.Module
    class Module
}
