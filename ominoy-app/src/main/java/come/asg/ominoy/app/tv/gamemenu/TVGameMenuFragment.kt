package come.asg.ominoy.app.tv.gamemenu

import android.os.Bundle
import android.view.View
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.lifecycle.Lifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import come.asg.ominoy.R
import come.asg.ominoy.app.shared.coreoptions.CoreOptionsPreferenceHelper
import come.asg.ominoy.app.shared.coreoptions.OminoyCoreOption
import come.asg.ominoy.app.shared.gamemenu.GameMenuHelper
import come.asg.ominoy.app.shared.input.InputDeviceManager
import come.asg.ominoy.common.coroutines.launchOnState
import come.asg.ominoy.common.coroutines.safeCollect
import come.asg.ominoy.lib.library.SystemCoreConfig
import come.asg.ominoy.lib.library.db.entity.Game
import come.asg.ominoy.lib.preferences.SharedPreferencesHelper
import come.asg.ominoy.lib.saves.StatesManager
import come.asg.ominoy.lib.saves.StatesPreviewManager

class TVGameMenuFragment(
    private val statesManager: StatesManager,
    private val statesPreviewManager: StatesPreviewManager,
    private val inputDeviceManager: InputDeviceManager,
    private val game: Game,
    private val systemCoreConfig: SystemCoreConfig,
    private val coreOptions: Array<OminoyCoreOption>,
    private val advancedCoreOptions: Array<OminoyCoreOption>,
    private val numDisks: Int,
    private val currentDisk: Int,
    private val audioEnabled: Boolean,
    private val fastForwardEnabled: Boolean,
    private val fastForwardSupported: Boolean,
) : LeanbackPreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        preferenceManager.preferenceDataStore =
            SharedPreferencesHelper.getSharedPreferencesDataStore(requireContext())
        setPreferencesFromResource(R.xml.tv_game_settings, rootKey)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        GameMenuHelper.setupAudioOption(preferenceScreen, audioEnabled)
        GameMenuHelper.setupFastForwardOption(preferenceScreen, fastForwardEnabled, fastForwardSupported)
        GameMenuHelper.setupSaveOption(preferenceScreen, systemCoreConfig)

        if (numDisks > 1) {
            GameMenuHelper.setupChangeDiskOption(activity, preferenceScreen, currentDisk, numDisks)
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeLoadAndSave()
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeControllers()
        }
    }

    private suspend fun initializeControllers() {
        inputDeviceManager.getGamePadsObservable()
            .safeCollect { setupCoreOptions(it.size) }
    }

    private fun setupCoreOptions(connectedGamePads: Int) {
        val coreOptionsScreen =
            findPreference<PreferenceScreen>(GameMenuHelper.SECTION_CORE_OPTIONS)
                ?: return

        coreOptionsScreen.removeAll()

        CoreOptionsPreferenceHelper.addPreferences(
            coreOptionsScreen,
            game.systemId,
            coreOptions.toList(),
            advancedCoreOptions.toList(),
        )

        CoreOptionsPreferenceHelper.addControllers(
            coreOptionsScreen,
            game.systemId,
            systemCoreConfig.coreID,
            connectedGamePads,
            systemCoreConfig.controllerConfigs,
        )
    }

    private suspend fun initializeLoadAndSave() {
        val saveScreen = findPreference<PreferenceScreen>(GameMenuHelper.SECTION_SAVE_GAME)
        val loadScreen = findPreference<PreferenceScreen>(GameMenuHelper.SECTION_LOAD_GAME)

        saveScreen?.isEnabled = systemCoreConfig.statesSupported
        loadScreen?.isEnabled = systemCoreConfig.statesSupported

        val slotsInfo = statesManager.getSavedSlotsInfo(game, systemCoreConfig.coreID)

        slotsInfo.forEachIndexed { index, saveInfo ->
            val bitmap =
                GameMenuHelper.getSaveStateBitmap(
                    requireContext(),
                    statesPreviewManager,
                    saveInfo,
                    game,
                    systemCoreConfig.coreID,
                    index,
                )

            if (saveScreen != null) {
                GameMenuHelper.addSavePreference(saveScreen, index, saveInfo, bitmap)
            }

            if (loadScreen != null) {
                GameMenuHelper.addLoadPreference(loadScreen, index, saveInfo, bitmap)
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (GameMenuHelper.onPreferenceTreeClicked(activity, preference)) {
            return true
        }
        return super.onPreferenceTreeClick(preference)
    }

    @dagger.Module
    class Module
}
