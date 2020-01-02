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

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
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
import javax.inject.Inject

class GameActivity : RetrogradeActivity() {
    companion object {
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
        retroGameView.setOnLongClickListener {
            displayOptionsMenu()
            true
        }

        gameViewLayout.addView(retroGameView)

        getAndResetTransientSaveRAMState()?.let {
            retroGameView.unserializeSRAM(it)
        }

        getAndResetTransientQuickSave()?.let {
            restoreQuickSaveAsync(it)
        }

        setupTouchInput(systemId)
        retroGameView.getConnectedGamepads()
                .autoDispose(scope())
                .subscribe { padLayout.updateVisibility(it == 0) }

        handleOrientationChange(resources.configuration.orientation)

        retroGameView.requestFocus()
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
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setGameViewAspectRatio("1:1")
        } else {
            setGameViewAspectRatio(null)
        }
    }

    private fun setGameViewAspectRatio(aspectRatio: String?) {
        val set = ConstraintSet()
        set.clone(containerLayout)
        set.setDimensionRatio(gameViewLayout.id, aspectRatio)
        set.applyTo(containerLayout)
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
        while (!retroGameView.unserialize(saveGame) && times > 0) {
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

    private fun setupTouchInput(systemId: String) {
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
    }

    private fun displayOptionsMenu() {
        val items = arrayOf(
                getString(R.string.quick_save),
                getString(R.string.quick_load),
                getString(R.string.reset),
                getString(R.string.close_without_saving)
        )

        val builder = AlertDialog.Builder(this)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> quickSave().subscribeOn(Schedulers.io()).autoDispose(scope()).subscribe()
                    1 -> quickLoad().subscribeOn(Schedulers.io()).autoDispose(scope()).subscribe()
                    2 -> reset()
                    3 -> finish()
                }
            }

        builder.create().show()
    }

    private fun performHapticFeedback(view: View) {
        val flags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, flags)
    }

    override fun onBackPressed() {
        saveAndFinish()
            .subscribeOn(Schedulers.io())
            .autoDispose(scope())
            .subscribe()
    }

    private fun saveAndFinish(): Completable {
        return Completable.fromCallable {
            saveSRAM().blockingAwait()
            quickSave().blockingAwait()

            setResult(Activity.RESULT_OK, Intent())
            finish()
        }
    }

    private fun quickSave(): Completable {
        return retrieveCurrentGame()
                .doOnSuccess { game ->
                    val data = retroGameView.serialize()
                    Timber.i("Storing quicksave with size: ${data.size}")
                    savesManager.setQuickSave(game, data).blockingAwait()
                }
                .ignoreElement()
    }

    private fun quickLoad(): Completable {
        return retrieveCurrentGame()
                .flatMapMaybe { savesManager.getQuickSave(it) }
                .doOnSuccess { retroGameView.unserialize(it) }
                .ignoreElement()
    }

    private fun saveSRAM(): Completable {
        return retrieveCurrentGame()
                .doOnSuccess {
                    val data = retroGameView.serializeSRAM()
                    if (data.isNotEmpty()) {
                        Timber.i("Storing sram with size: ${data.size}")
                        savesManager.setSaveRAM(it, data).blockingAwait()
                    }
                }
                .ignoreElement()
    }

    private fun retrieveCurrentGame(): Single<Game> {
        val gameId = intent.getIntExtra(EXTRA_GAME_ID, -1)
        return retrogradeDb.gameDao().selectById(gameId).toSingle()
    }

    private fun reset() {
        retroGameView.reset()
    }
}
