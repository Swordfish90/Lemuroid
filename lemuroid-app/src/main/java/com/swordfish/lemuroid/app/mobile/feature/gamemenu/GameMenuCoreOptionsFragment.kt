package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOptionsPreferenceHelper
import com.swordfish.lemuroid.app.shared.coreoptions.LemuroidCoreOption
import com.swordfish.lemuroid.app.shared.input.FlowInputDeviceManager
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import dagger.android.support.AndroidSupportInjection
import java.security.InvalidParameterException
import javax.inject.Inject

class GameMenuCoreOptionsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var inputDeviceManager: FlowInputDeviceManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore =
            SharedPreferencesHelper.getSharedPreferencesDataStore(requireContext())
        addPreferencesFromResource(R.xml.empty_preference_screen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchOnState(Lifecycle.State.CREATED) {
            inputDeviceManager
                .getGamePadsObservable()
                .collect { updateScreen(it.size) }
        }
    }

    private fun updateScreen(connectedGamePads: Int) {
        preferenceScreen.removeAll()

        val extras = activity?.intent?.extras

        val coreOptions = extras?.getSerializable(GameMenuContract.EXTRA_CORE_OPTIONS) as Array<LemuroidCoreOption>?
            ?: throw InvalidParameterException("Missing EXTRA_CORE_OPTIONS")

        val advancedCoreOptions = extras?.getSerializable(
            GameMenuContract.EXTRA_ADVANCED_CORE_OPTIONS
        ) as Array<LemuroidCoreOption>?
            ?: throw InvalidParameterException("Missing EXTRA_ADVANCED_CORE_OPTIONS")

        val game = extras?.getSerializable(GameMenuContract.EXTRA_GAME) as Game?
            ?: throw InvalidParameterException("Missing EXTRA_GAME")

        val coreConfig = extras?.getSerializable(GameMenuContract.EXTRA_SYSTEM_CORE_CONFIG) as SystemCoreConfig?
            ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_CORE_CONFIG")

        CoreOptionsPreferenceHelper.addPreferences(
            preferenceScreen,
            game.systemId,
            coreOptions.toList(),
            advancedCoreOptions.toList()
        )

        CoreOptionsPreferenceHelper.addControllers(
            preferenceScreen,
            game.systemId,
            coreConfig.coreID,
            maxOf(1, connectedGamePads),
            coreConfig.controllerConfigs
        )
    }

    @dagger.Module
    class Module
}
