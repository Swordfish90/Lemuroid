package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.gamemenu.GameMenuHelper
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.common.preferences.DummyDataStore
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import dagger.android.support.AndroidSupportInjection
import java.security.InvalidParameterException
import javax.inject.Inject

class GameMenuLoadFragment : PreferenceFragmentCompat() {

    @Inject lateinit var statesManager: StatesManager
    @Inject lateinit var statesPreviewManager: StatesPreviewManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DummyDataStore
        addPreferencesFromResource(R.xml.empty_preference_screen)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = activity?.intent?.extras

        val game = extras?.getSerializable(GameMenuContract.EXTRA_GAME) as Game?
            ?: throw InvalidParameterException("Missing EXTRA_GAME")

        val systemCoreConfig = extras?.getSerializable(GameMenuContract.EXTRA_SYSTEM_CORE_CONFIG) as SystemCoreConfig?
            ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_CORE_CONFIG")

        launchOnState(Lifecycle.State.CREATED) {
            setupLoadPreference(game, systemCoreConfig)
        }
    }

    private suspend fun setupLoadPreference(game: Game, systemCoreConfig: SystemCoreConfig) {
        val slotsInfo = statesManager.getSavedSlotsInfo(game, systemCoreConfig.coreID)

        slotsInfo.forEachIndexed { index, saveInfo ->
            val bitmap = GameMenuHelper.getSaveStateBitmap(
                requireContext(),
                statesPreviewManager,
                saveInfo,
                game,
                systemCoreConfig.coreID,
                index
            )

            GameMenuHelper.addLoadPreference(
                preferenceScreen,
                index,
                saveInfo,
                bitmap
            )
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return GameMenuHelper.onPreferenceTreeClicked(activity, preference)
    }

    @dagger.Module
    class Module
}
