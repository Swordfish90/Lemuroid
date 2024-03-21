package com.swordfish.lemuroid.app.shared.gamemenu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.common.graphics.GraphicsUtils
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SaveInfo
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import java.text.SimpleDateFormat
import kotlin.math.roundToInt

object GameMenuHelper {
    fun setupAudioOption(
        screen: PreferenceScreen,
        audioEnabled: Boolean,
    ) {
        val preference = screen.findPreference<SwitchPreference>(MUTE)
        preference?.isChecked = !audioEnabled
    }

    fun setupFastForwardOption(
        screen: PreferenceScreen,
        fastForwardEnabled: Boolean,
        fastForwardSupported: Boolean,
    ) {
        val preference = screen.findPreference<SwitchPreference>(FAST_FORWARD)
        preference?.isChecked = fastForwardEnabled
        preference?.isVisible = fastForwardSupported
    }

    fun setupSaveOption(
        screen: PreferenceScreen,
        systemCoreConfig: SystemCoreConfig,
    ) {
        val savesOption = screen.findPreference<Preference>(SECTION_SAVE_GAME)
        savesOption?.isVisible = systemCoreConfig.statesSupported

        val loadOption = screen.findPreference<Preference>(SECTION_LOAD_GAME)
        loadOption?.isVisible = systemCoreConfig.statesSupported
    }

    fun setupSettingsOption(
        screen: PreferenceScreen,
        systemCoreConfig: SystemCoreConfig,
    ) {
        screen.findPreference<Preference>(SECTION_CORE_OPTIONS)?.isVisible =
            sequenceOf(
                systemCoreConfig.exposedSettings.isNotEmpty(),
                systemCoreConfig.exposedAdvancedSettings.isNotEmpty(),
                systemCoreConfig.controllerConfigs.values.any { it.size > 1 },
            ).any { it }
    }

    fun setupChangeDiskOption(
        activity: Activity?,
        screen: PreferenceScreen,
        currentDisk: Int,
        numDisks: Int,
    ) {
        val changeDiskPreference = screen.findPreference<ListPreference>(SECTION_CHANGE_DISK)
        changeDiskPreference?.isVisible = numDisks > 1

        changeDiskPreference?.entries =
            (0 until numDisks)
                .map {
                    screen.context.resources.getString(
                        R.string.game_menu_change_disk_disk,
                        (it + 1).toString(),
                    )
                }
                .toTypedArray()

        changeDiskPreference?.entryValues =
            (0 until numDisks)
                .map { it.toString() }
                .toTypedArray()

        changeDiskPreference?.setValueIndex(currentDisk)
        changeDiskPreference?.setOnPreferenceChangeListener { _, newValue ->
            val resultIntent =
                Intent().apply {
                    putExtra(GameMenuContract.RESULT_CHANGE_DISK, (newValue as String).toInt())
                }
            setResultAndFinish(activity, resultIntent)
            true
        }
    }

    fun addSavePreference(
        screen: PreferenceScreen,
        index: Int,
        saveStateInfo: SaveInfo,
        bitmap: Bitmap?,
    ) {
        screen.addPreference(
            Preference(screen.context).apply {
                this.key = "pref_game_save_$index"
                this.summary = getSaveStateDescription(saveStateInfo)
                this.title = context.getString(R.string.game_menu_state, (index + 1).toString())
                this.icon = BitmapDrawable(screen.context.resources, bitmap)
            },
        )
    }

    fun addLoadPreference(
        screen: PreferenceScreen,
        index: Int,
        saveStateInfo: SaveInfo,
        bitmap: Bitmap?,
    ) {
        screen.addPreference(
            Preference(screen.context, null).apply {
                this.key = "pref_game_load_$index"
                this.summary = getSaveStateDescription(saveStateInfo)
                this.isEnabled = saveStateInfo.exists
                this.title = context.getString(R.string.game_menu_state, (index + 1).toString())
                this.icon = BitmapDrawable(screen.context.resources, bitmap)
            },
        )
    }

    fun onPreferenceTreeClicked(
        activity: Activity?,
        preference: Preference?,
    ): Boolean {
        return when (preference?.key) {
            "pref_game_save_0" -> handleSaveAction(activity, 0)
            "pref_game_save_1" -> handleSaveAction(activity, 1)
            "pref_game_save_2" -> handleSaveAction(activity, 2)
            "pref_game_save_3" -> handleSaveAction(activity, 3)
            "pref_game_load_0" -> handleLoadAction(activity, 0)
            "pref_game_load_1" -> handleLoadAction(activity, 1)
            "pref_game_load_2" -> handleLoadAction(activity, 2)
            "pref_game_load_3" -> handleLoadAction(activity, 3)
            "pref_game_mute" -> {
                val currentValue = (preference as SwitchPreference).isChecked
                val resultIntent =
                    Intent().apply {
                        putExtra(GameMenuContract.RESULT_ENABLE_AUDIO, !currentValue)
                    }
                setResultAndFinish(activity, resultIntent)
                true
            }
            "pref_game_fast_forward" -> {
                val currentValue = (preference as SwitchPreference).isChecked
                val resultIntent =
                    Intent().apply {
                        putExtra(GameMenuContract.RESULT_ENABLE_FAST_FORWARD, currentValue)
                    }
                setResultAndFinish(activity, resultIntent)
                true
            }
            "pref_game_reset" -> {
                val resultIntent =
                    Intent().apply {
                        putExtra(GameMenuContract.RESULT_RESET, true)
                    }
                setResultAndFinish(activity, resultIntent)
                true
            }
            "pref_game_quit" -> {
                val resultIntent =
                    Intent().apply {
                        putExtra(GameMenuContract.RESULT_QUIT, true)
                    }
                setResultAndFinish(activity, resultIntent)
                true
            }
            "pref_game_edit_touch_controls" -> {
                val resultIntent =
                    Intent().apply {
                        putExtra(GameMenuContract.RESULT_EDIT_TOUCH_CONTROLS, true)
                    }
                setResultAndFinish(activity, resultIntent)
                true
            }
            else -> false
        }
    }

    private fun handleSaveAction(
        activity: Activity?,
        index: Int,
    ): Boolean {
        val resultIntent =
            Intent().apply {
                putExtra(GameMenuContract.RESULT_SAVE, index)
            }
        setResultAndFinish(activity, resultIntent)
        return true
    }

    private fun handleLoadAction(
        activity: Activity?,
        index: Int,
    ): Boolean {
        val resultIntent =
            Intent().apply {
                putExtra(GameMenuContract.RESULT_LOAD, index)
            }
        setResultAndFinish(activity, resultIntent)
        return true
    }

    private fun setResultAndFinish(
        activity: Activity?,
        resultIntent: Intent,
    ) {
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        activity?.finish()
    }

    fun getSaveStateDescription(saveInfo: SaveInfo): String {
        val formatter = SimpleDateFormat.getDateTimeInstance()
        return if (saveInfo.exists) {
            formatter.format(saveInfo.date)
        } else {
            ""
        }
    }

    suspend fun getSaveStateBitmap(
        context: Context,
        statesPreviewManager: StatesPreviewManager,
        saveStateInfo: SaveInfo,
        game: Game,
        coreID: CoreID,
        index: Int,
    ): Bitmap? {
        if (!saveStateInfo.exists) return null
        val imageSize = GraphicsUtils.convertDpToPixel(96f, context).roundToInt()
        return statesPreviewManager.getPreviewForSlot(game, coreID, index, imageSize)
    }

    const val FAST_FORWARD = "pref_game_fast_forward"
    const val MUTE = "pref_game_mute"
    const val SECTION_CORE_OPTIONS = "pref_game_section_core_options"
    const val SECTION_CHANGE_DISK = "pref_game_section_change_disk"
    const val SECTION_SAVE_GAME = "pref_game_section_save"
    const val SECTION_LOAD_GAME = "pref_game_section_load"
}
