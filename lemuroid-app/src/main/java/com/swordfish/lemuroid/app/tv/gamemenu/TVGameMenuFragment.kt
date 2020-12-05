package com.swordfish.lemuroid.app.tv.gamemenu

import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOption
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOptionsPreferenceHelper
import com.swordfish.lemuroid.app.shared.gamemenu.GameMenuHelper
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers

class TVGameMenuFragment(
    private val statesManager: StatesManager,
    private val statesPreviewManager: StatesPreviewManager,
    private val game: Game,
    private val systemCoreConfig: SystemCoreConfig,
    private val coreOptions: Array<CoreOption>,
    private val numDisks: Int,
    private val currentDisk: Int,
    private val audioEnabled: Boolean,
    private val fastForwardEnabled: Boolean,
    private val fastForwardSupported: Boolean
) : LeanbackPreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.tv_game_settings, rootKey)
        setupCoreOptions()
        setupLoadAndSave()

        GameMenuHelper.setupAudioOption(preferenceScreen, audioEnabled)
        GameMenuHelper.setupFastForwardOption(preferenceScreen, fastForwardEnabled, fastForwardSupported)

        if (numDisks > 1) {
            GameMenuHelper.setupChangeDiskOption(activity, preferenceScreen, currentDisk, numDisks)
        }
    }

    private fun setupCoreOptions() {
        val coreOptionsScreen = findPreference<PreferenceScreen>(GameMenuHelper.SECTION_CORE_OPTIONS)
        coreOptionsScreen?.isVisible = coreOptions.isNotEmpty()
        coreOptions
            .map {
                CoreOptionsPreferenceHelper.convertToPreference(
                    preferenceScreen.context,
                    it,
                    game.systemId
                )
            }
            .forEach { coreOptionsScreen?.addPreference(it) }
    }

    private fun setupLoadAndSave() {
        val saveScreen = findPreference<PreferenceScreen>(GameMenuHelper.SECTION_SAVE_GAME)
        val loadScreen = findPreference<PreferenceScreen>(GameMenuHelper.SECTION_LOAD_GAME)

        saveScreen?.isEnabled = systemCoreConfig.statesSupported
        loadScreen?.isEnabled = systemCoreConfig.statesSupported

        Single.just(game)
            .flatMap {
                Singles.zip(
                    statesManager.getSavedSlotsInfo(it, systemCoreConfig.coreID),
                    statesPreviewManager.getPreviewsForSlots(it, systemCoreConfig.coreID)
                )
            }
            .map { (states, previews) -> states.zip(previews) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribeBy {
                it.forEachIndexed { index, (saveInfos, previewFile) ->
                    if (saveScreen != null)
                        GameMenuHelper.addSavePreference(saveScreen, index, saveInfos, previewFile)

                    if (loadScreen != null)
                        GameMenuHelper.addLoadPreference(loadScreen, index, saveInfos, previewFile)
                }
            }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (GameMenuHelper.onPreferenceTreeClicked(activity, preference))
            return true
        return super.onPreferenceTreeClick(preference)
    }

    @dagger.Module
    class Module
}
