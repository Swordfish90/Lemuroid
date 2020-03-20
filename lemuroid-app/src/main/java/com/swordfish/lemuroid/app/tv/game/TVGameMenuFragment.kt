package com.swordfish.lemuroid.app.tv.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOption
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOptionsPreferenceHelper
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat

class TVGameMenuFragment(
    private val retrogradeDb: RetrogradeDatabase,
    private val savesManager: SavesManager,
    private val gameId: Int,
    private val systemId: String,
    private val coreOptions: Array<CoreOption>,
    private val numDisks: Int
) : LeanbackPreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.tv_game_settings, rootKey)
        setupCoreOptions()
        setupLoadAndSave()
        setupChangeDiskOption()
    }

    private fun setupCoreOptions() {
        val coreOptionsScreen = findPreference<PreferenceScreen>(SECTION_CORE_OPTIONS)
        coreOptionsScreen?.isVisible = coreOptions.isNotEmpty()
        coreOptions
                .map { CoreOptionsPreferenceHelper.convertToPreference(preferenceScreen.context, it, systemId) }
                .forEach { coreOptionsScreen?.addPreference(it) }
    }

    private fun setupChangeDiskOption() {
        val changeDiskPreference = findPreference<PreferenceScreen>(SECTION_CHANGE_DISK)
        changeDiskPreference?.isVisible = numDisks > 1
        (0 until numDisks)
                .map {
                    val preference = Preference(requireContext())
                    preference.key = "pref_game_disk_$it"
                    preference.title = resources.getString(R.string.game_menu_change_disk_disk, (it + 1).toString())
                    preference
                }
                .forEach { changeDiskPreference?.addPreference(it) }
    }

    private fun setupLoadAndSave() {
        val saveScreen = findPreference<PreferenceScreen>(SECTION_SAVE_GAME)
        val loadScreen = findPreference<PreferenceScreen>(SECTION_LOAD_GAME)

        retrieveCurrentGame()
                .flatMapSingle { savesManager.getSavedSlotsInfo(it, GameSystem.findById(it.systemId).coreName) }
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope())
                .subscribeBy {
                    it.forEachIndexed { index, saveInfos ->
                        addSaveAndLoadPreferences(saveScreen, loadScreen, index, saveInfos)
                    }
                }
    }

    private fun addSaveAndLoadPreferences(
        saveScreen: PreferenceScreen?,
        loadScreen: PreferenceScreen?,
        index: Int,
        saveInfos: SavesManager.SaveInfos
    ) {
        saveScreen?.addPreference(
            Preference(requireContext()).apply {
                this.key = "pref_game_save_$index"
                this.summary = getDateString(saveInfos)
                this.title = getString(R.string.game_menu_state, (index + 1).toString())
            }
        )
        loadScreen?.addPreference(
            Preference(requireContext()).apply {
                this.key = "pref_game_load_$index"
                this.summary = getDateString(saveInfos)
                this.isEnabled = saveInfos.exists
                this.title = getString(R.string.game_menu_state, (index + 1).toString())
            }
        )
    }

    private fun getDateString(saveInfo: SavesManager.SaveInfos): String {
        val formatter = SimpleDateFormat.getDateTimeInstance()
        return if (saveInfo.exists) {
            formatter.format(saveInfo.date)
        } else {
            ""
        }
    }

    private fun retrieveCurrentGame(): Maybe<Game> {
        return retrogradeDb.gameDao()
            .selectById(gameId)
            .subscribeOn(Schedulers.io())
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key?.startsWith("pref_game_disk_") == true) {
            handleChangeDisk(preference.key.replace("pref_game_disk_", "").toInt())
            return true
        }
        when (preference?.key) {
            "pref_game_reset" -> {
                val resultIntent = Intent().apply {
                    putExtra(GameMenuContract.RESULT_RESET, true)
                }
                activity?.setResult(Activity.RESULT_OK, resultIntent)
                activity?.finish()
            }
            "pref_game_quit" -> {
                val resultIntent = Intent().apply {
                    putExtra(GameMenuContract.RESULT_QUIT, true)
                }
                activity?.setResult(Activity.RESULT_OK, resultIntent)
                activity?.finish()
            }
            "pref_game_save_0" -> handleSaveAction(0)
            "pref_game_save_1" -> handleSaveAction(1)
            "pref_game_save_2" -> handleSaveAction(2)
            "pref_game_save_3" -> handleSaveAction(3)

            "pref_game_load_0" -> handleLoadAction(0)
            "pref_game_load_1" -> handleLoadAction(1)
            "pref_game_load_2" -> handleLoadAction(2)
            "pref_game_load_3" -> handleLoadAction(3)
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun handleChangeDisk(index: Int) {
        val resultIntent = Intent().apply {
            putExtra(GameMenuContract.RESULT_CHANGE_DISK, index)
        }
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        activity?.finish()
    }

    private fun handleSaveAction(index: Int) {
        val resultIntent = Intent().apply {
            putExtra(GameMenuContract.RESULT_SAVE, index)
        }
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        activity?.finish()
    }

    private fun handleLoadAction(index: Int) {
        val resultIntent = Intent().apply {
            putExtra(GameMenuContract.RESULT_LOAD, index)
        }
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        activity?.finish()
    }

    companion object {
        private const val SECTION_CORE_OPTIONS = "pref_game_section_core_options"
        private const val SECTION_CHANGE_DISK = "pref_game_section_change_disk"
        private const val SECTION_SAVE_GAME = "pref_game_section_save"
        private const val SECTION_LOAD_GAME = "pref_game_section_load"
    }

    @dagger.Module
    class Module
}
