package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOption
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOptionsPreferenceHelper
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import java.security.InvalidParameterException

class GameMenuCoreOptionsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore =
            SharedPreferencesHelper.getSharedPreferencesDataStore(requireContext())
        addPreferencesFromResource(R.xml.empty_preference_screen)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = activity?.intent?.extras

        val coreOptions = extras?.getSerializable(GameMenuContract.EXTRA_CORE_OPTIONS) as Array<CoreOption>?
            ?: throw InvalidParameterException("Missing EXTRA_CORE_OPTIONS")

        val advancedCoreOptions = extras?.getSerializable(GameMenuContract.EXTRA_ADVANCED_CORE_OPTIONS) as Array<CoreOption>?
            ?: throw InvalidParameterException("Missing EXTRA_ADVANCED_CORE_OPTIONS")

        val game = extras?.getSerializable(GameMenuContract.EXTRA_GAME) as Game?
            ?: throw InvalidParameterException("Missing EXTRA_GAME")

        CoreOptionsPreferenceHelper.addPreferences(
            preferenceScreen,
            game.systemId,
            coreOptions.toList(),
            advancedCoreOptions.toList()
        )
    }

    @dagger.Module
    class Module
}
