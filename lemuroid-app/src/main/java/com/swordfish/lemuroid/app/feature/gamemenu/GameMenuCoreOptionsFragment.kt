package com.swordfish.lemuroid.app.feature.gamemenu

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.feature.coreoptions.CoreOption
import com.swordfish.lemuroid.app.feature.coreoptions.CoreOptionsPreferenceHelper
import java.security.InvalidParameterException

class GameMenuCoreOptionsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.core_preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val coreOptions = arguments?.getSerializable(GameMenuContract.EXTRA_CORE_OPTIONS) as Array<CoreOption>?
                ?: throw InvalidParameterException("Missing EXTRA_CORE_OPTIONS")

        val systemID = arguments?.getString(GameMenuContract.EXTRA_SYSTEM_ID)
                ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_ID")

        coreOptions
            .map { CoreOptionsPreferenceHelper.convertToPreference(preferenceScreen.context, it, systemID) }
            .forEach { preferenceScreen.addPreference(it) }
    }

    @dagger.Module
    class Module
}
