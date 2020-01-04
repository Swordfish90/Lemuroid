/*
 * GameActivity.kt
 *
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.feature.game

import android.app.Dialog
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.swordfish.lemuroid.lib.android.RetrogradeActivity
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.touchinput.pads.GamePadFactory
import com.uber.autodispose.android.lifecycle.scope
import timber.log.Timber
import com.swordfish.lemuroid.R
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.touchinput.events.PadEvent
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.lang.Thread.sleep
import androidx.constraintlayout.widget.ConstraintSet
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SavesManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import com.swordfish.lemuroid.lib.ui.updateVisibility
import com.uber.autodispose.autoDispose
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.math.roundToInt

class GameActivity : RetrogradeActivity() {
    companion object {
        // TODO: We should handle gamepads without these buttons
        val GAMEPAD_MENU_SHORTCUT = setOf(KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR)

        const val EXTRA_GAME_ID = "game_id"
        const val EXTRA_SYSTEM_ID = "system_id"
        const val EXTRA_CORE_PATH = "core_path"
        const val EXTRA_GAME_PATH = "game_path"

        private var transientStashedState: ByteArray? = null
        private var transientSRAMState: ByteArray? = null

        /** A full savestate may not fit a bundle, so we need to ask for forgiveness and pass it statically. */
        fun setTransientQuickSave(state: ByteArray?) {
            transientStashedState = state
        }

        fun setTransientSaveRAMState(data: ByteArray?) {
            transientSRAMState = data
        }

        fun getAndResetTransientQuickSave(): ByteArray? {
            val result = transientStashedState
            transientStashedState = null
            return result
        }

        fun getAndResetTransientSaveRAMState(): ByteArray? {
            val result = transientSRAMState
            transientSRAMState = null
            return result
        }
    }

    private lateinit var containerLayout: ConstraintLayout
    private lateinit var gameViewLayout: FrameLayout
    private lateinit var padLayout: FrameLayout
    private lateinit var menuButton: ImageButton

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var retroGameView: GLRetroView

    @Inject lateinit var savesManager: SavesManager
    @Inject lateinit var retrogradeDb: RetrogradeDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        containerLayout = findViewById(R.id.game_container)
        gameViewLayout = findViewById(R.id.gameview_layout)
        padLayout = findViewById(R.id.pad_layout)
        menuButton = findViewById(R.id.menu_button)

        val systemId = intent.getStringExtra(EXTRA_SYSTEM_ID)

        val directoriesManager = DirectoriesManager(applicationContext)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val useShaders = sharedPreferences.getBoolean(getString(R.string.pref_key_shader), true)

        retroGameView = GLRetroView(
            this,
            intent.getStringExtra(EXTRA_CORE_PATH),
            intent.getStringExtra(EXTRA_GAME_PATH),
            directoriesManager.getSystemDirectory().absolutePath,
            directoriesManager.getSavesDirectory().absolutePath,
            getShaderForSystem(useShaders, systemId)
        )

        retroGameView.onCreate()

        gameViewLayout.addView(retroGameView)

        getAndResetTransientSaveRAMState()?.let {
            retroGameView.unserializeSRAM(it)
        }

        getAndResetTransientQuickSave()?.let {
            restoreQuickSaveAsync(it)
        }

        setupVirtualPad(systemId)

        setupPhysicalPad()

        handleOrientationChange(resources.configuration.orientation)

        retroGameView.requestFocus()
    }

    private fun displayOptionsDialog() {
        ContextGameDialog()
            .displayOptionsDialog()
            .autoDispose(scope()).subscribe()
    }

    private fun getShaderForSystem(useShader: Boolean, systemId: String): Int {
        if (!useShader) {
            return GLRetroView.SHADER_DEFAULT
        }

        return when (systemId) {
            GameSystem.GBA_ID -> GLRetroView.SHADER_LCD
            GameSystem.GBC_ID -> GLRetroView.SHADER_LCD
            GameSystem.GB_ID -> GLRetroView.SHADER_LCD
            GameSystem.N64_ID -> GLRetroView.SHADER_CRT
            GameSystem.GENESIS_ID -> GLRetroView.SHADER_CRT
            GameSystem.NES_ID -> GLRetroView.SHADER_CRT
            GameSystem.SNES_ID -> GLRetroView.SHADER_CRT
            GameSystem.ARCADE_ID -> GLRetroView.SHADER_CRT
            else -> GLRetroView.SHADER_DEFAULT
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleOrientationChange(newConfig.orientation)
    }

    private fun handleOrientationChange(orientation: Int) {
        OrientationHandler().handleOrientationChange(orientation)
    }

    /* On some cores unserialize fails with no reason. So we need to try multiple times. */
    private fun restoreQuickSaveAsync(saveGame: ByteArray) {
        Timber.i("Loading saved state of ${saveGame.size} bytes")

        getRetryRestoreQuickSave(saveGame)
            .subscribeOn(Schedulers.io())
            .autoDispose(scope())
            .subscribe()
    }

    override fun onResume() {
        super.onResume()
        retroGameView.onResume()
    }

    override fun onPause() {
        super.onPause()
        retroGameView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        retroGameView.onDestroy()
    }

    private fun getRetryRestoreQuickSave(saveGame: ByteArray) = Completable.fromCallable {
        var times = 10
        while (!retroGameView.unserializeState(saveGame) && times > 0) {
            sleep(200)
            times--
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }

    private fun setupVirtualPad(systemId: String) {
        val gamePadLayout = when (systemId) {
            in listOf(GameSystem.GBA_ID) -> GamePadFactory.Layout.GBA
            in listOf(GameSystem.SNES_ID) -> GamePadFactory.Layout.SNES
            in listOf(GameSystem.NES_ID, GameSystem.GB_ID, GameSystem.GBC_ID) -> GamePadFactory.Layout.NES
            in listOf(GameSystem.GENESIS_ID) -> GamePadFactory.Layout.GENESIS
            in listOf(GameSystem.N64_ID) -> GamePadFactory.Layout.N64
            else -> GamePadFactory.Layout.PSX
        }

        val gameView = gamePadLayout.let {
            GamePadFactory.getGamePadView(this, it)
        }

        padLayout.addView(gameView)

        gameView.getEvents()
            .subscribeOn(Schedulers.computation())
            .doOnNext {
                if (it.haptic) {
                    performHapticFeedback(gameView)
                }
            }
            .autoDispose(scope())
            .subscribe {
                when (it) {
                    is PadEvent.Button -> retroGameView.sendKeyEvent(it.action, it.keycode)
                    is PadEvent.Stick -> retroGameView.sendMotionEvent(it.source, it.xAxis, it.yAxis)
                }
            }

        retroGameView.getConnectedGamepads()
            .autoDispose(scope())
            .subscribe {
                padLayout.updateVisibility(it == 0)
                menuButton.updateVisibility(it == 0)
            }

        menuButton.setOnClickListener {
            performHapticFeedback(it)
            displayOptionsDialog()
        }
    }

    private fun setupPhysicalPad() {
        retroGameView.getGameKeyEvents()
            .filter { it.keyCode in GAMEPAD_MENU_SHORTCUT }
            .scan(mutableSetOf<Int>()) { keys, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    keys.add(event.keyCode)
                } else if (event.action == KeyEvent.ACTION_UP) {
                    keys.remove(event.keyCode)
                }
                keys
            }
            .doOnNext {
                if (it.containsAll(GAMEPAD_MENU_SHORTCUT)) {
                    displayOptionsDialog()
                }
            }
            .subscribeOn(Schedulers.single())
            .autoDispose(scope())
            .subscribe()

        retroGameView.getConnectedGamepads()
            .map { it > 0 }
            .distinctUntilChanged()
            .autoDispose(scope())
            .subscribe { gamepadsConnected ->
                if (gamepadsConnected) {
                    val message = R.string.game_toast_settings_button_using_gamepad
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun performHapticFeedback(view: View) {
        val flags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, flags)
    }

    override fun onBackPressed() {
        autoSaveAndFinish()
    }

    private fun autoSaveAndFinish() {
        getAutoSaveAndFinishCompletable()
                .subscribeOn(Schedulers.io())
                .autoDispose(scope())
                .subscribe()
    }

    private fun getAutoSaveAndFinishCompletable(): Completable {
        return retrieveCurrentGame().flatMapCompletable { game ->
            val saveRAMData = retroGameView.serializeSRAM()
            val autoSaveData = retroGameView.serializeState()

            val autoSaveCompletable = savesManager.setAutoSave(game, autoSaveData)
                    .doOnComplete { Timber.i("Stored autosave file with size: ${autoSaveData.size}") }

            val saveRAMCompletable = savesManager.setSaveRAM(game, saveRAMData)
                    .doOnComplete { Timber.i("Stored sram file with size: ${saveRAMData.size}") }

            saveRAMCompletable.andThen(autoSaveCompletable)
                    .doOnComplete { finish() }
        }
    }

    private fun saveSlot(index: Int): Completable {
        return retrieveCurrentGame()
            .map { it to retroGameView.serializeState() }
            .doOnSuccess { (_, data) -> Timber.i("Storing quicksave with size: ${data.size}") }
            .flatMapCompletable { (game, data) -> savesManager.setSlotSave(game, data, index) }
    }

    private fun loadSlot(index: Int): Completable {
        return retrieveCurrentGame()
            .flatMapMaybe { savesManager.getSlotSave(it, index) }
            .doOnSuccess { retroGameView.unserializeState(it) }
            .ignoreElement()
    }

    private fun retrieveCurrentGame(): Single<Game> {
        val gameId = intent.getIntExtra(EXTRA_GAME_ID, -1)
        return retrogradeDb.gameDao()
            .selectById(gameId).toSingle()
            .subscribeOn(Schedulers.io())
    }

    private fun reset() {
        retroGameView.reset()
    }

    inner class OrientationHandler {

        fun handleOrientationChange(orientation: Int) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {

                // When in portrait mode we don't want the GLSurfaceView to fill the entire screen. We want it on top.
                setGameViewAspectRatio("1:1")

                // We should also add some padding the virtual layout or it would clash with the menu button.
                setVirtualPadBottomPadding(resources.getDimension(R.dimen.game_menu_button_size).roundToInt())

                // Finally we should also avoid system bars. Touch element might appear under system bars, or the game
                // view might be cut due to rounded corners.
                setContainerWindowsInsets(true, true)
            } else {
                setGameViewAspectRatio(null)
                setVirtualPadBottomPadding(0)
                setContainerWindowsInsets(top = false, bottom = true)
            }
        }

        private fun setVirtualPadBottomPadding(bottomPadding: Int) {
            padLayout.setPadding(0, 0, 0, bottomPadding)
        }

        private fun setGameViewAspectRatio(aspectRatio: String?) {
            val set = ConstraintSet()
            set.clone(containerLayout)
            set.setDimensionRatio(gameViewLayout.id, aspectRatio)
            set.applyTo(containerLayout)
        }

        private fun setContainerWindowsInsets(top: Boolean, bottom: Boolean) {
            containerLayout.setOnApplyWindowInsetsListener { v, insets ->
                val topInset = if (top) { insets.systemWindowInsetTop } else { 0 }
                val bottomInset = if (bottom) { insets.systemWindowInsetBottom } else { 0 }
                v.setPadding(0, topInset, 0, bottomInset)
                insets.consumeSystemWindowInsets()
            }
            containerLayout.requestApplyInsets()
        }
    }

    // TODO: We should consider promoting it to an activity.
    inner class ContextGameDialog {
        fun displayOptionsDialog(): Completable {
            return this@GameActivity.retrieveCurrentGame()
                .flatMap { savesManager.getSavedSlotsInfo(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { presentContextDialog(it) }
                .ignoreElement()
        }

        private fun presentContextDialog(infos: List<SavesManager.SaveInfos>) {
            val dialog = Dialog(this@GameActivity)
            dialog.setContentView(R.layout.layout_game_dialog)

            val slot1SaveView = dialog.findViewById<View>(R.id.save_entry_slot1)
            val slot2SaveView = dialog.findViewById<View>(R.id.save_entry_slot2)
            val slot3SaveView = dialog.findViewById<View>(R.id.save_entry_slot3)
            val slot4SaveView = dialog.findViewById<View>(R.id.save_entry_slot4)

            setupQuickSaveView(dialog, slot1SaveView, 0, infos[0])
            setupQuickSaveView(dialog, slot2SaveView, 1, infos[1])
            setupQuickSaveView(dialog, slot3SaveView, 2, infos[2])
            setupQuickSaveView(dialog, slot4SaveView, 3, infos[3])

            dialog.findViewById<Button>(R.id.save_entry_reset).setOnClickListener {
                this@GameActivity.reset()
                dialog.dismiss()
            }

            dialog.findViewById<Button>(R.id.save_entry_close).setOnClickListener {
                this@GameActivity.autoSaveAndFinish()
                dialog.dismiss()
            }

            showImmersive(dialog)
        }

        /** This is required to workaround an android bug marked as Wont Fix :(.
         *  More details here: https://issuetracker.google.com/issues/36992828
         *  Stackoverflow solution: https://stackoverflow.com/questions/22794049/how-do-i-maintain-the-immersive-mode-in-dialogs
         */
        private fun showImmersive(dialog: Dialog) {
            val flag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            dialog.window?.setFlags(flag, flag)

            dialog.show()

            dialog.window?.decorView?.systemUiVisibility = window.decorView.systemUiVisibility

            dialog.window?.clearFlags(flag)
        }

        private fun setupQuickSaveView(
            dialog: Dialog,
            quickSaveView: View,
            index: Int,
            saveInfo: SavesManager.SaveInfos
        ) {
            val title = this@GameActivity.getString(R.string.game_dialog_state, (index + 1).toString())

            if (saveInfo.exists) {
                val formatter = SimpleDateFormat.getDateTimeInstance()
                val date = formatter.format(saveInfo.date)
                quickSaveView.findViewById<TextView>(R.id.game_dialog_entry_subtext).text = date
            }

            quickSaveView.findViewById<TextView>(R.id.game_dialog_entry_text).text = title
            quickSaveView.findViewById<Button>(R.id.game_dialog_entry_load).apply {
                this.isEnabled = saveInfo.exists
                this.setOnClickListener {
                    loadSlot(index).autoDispose(scope()).subscribe()
                    dialog.dismiss()
                }
            }

            quickSaveView.findViewById<Button>(R.id.game_dialog_entry_save).apply {
                this.setOnClickListener {
                    saveSlot(index).autoDispose(scope()).subscribe()
                    dialog.dismiss()
                }
            }
        }
    }
}
