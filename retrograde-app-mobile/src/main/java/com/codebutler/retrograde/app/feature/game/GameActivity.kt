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

package com.codebutler.retrograde.app.feature.game

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.codebutler.retrograde.common.kotlin.bindView
import com.codebutler.retrograde.lib.android.RetrogradeActivity
import com.codebutler.retrograde.lib.game.GameLoader
import com.codebutler.retrograde.lib.game.audio.GameAudio
import com.codebutler.retrograde.lib.game.display.GameDisplay
import com.codebutler.retrograde.lib.game.display.gl.GlGameDisplay
import com.codebutler.retrograde.lib.game.display.shaders.RetroShader
import com.codebutler.retrograde.lib.game.input.GameInput
import com.codebutler.retrograde.lib.library.GameSystem
import com.codebutler.retrograde.lib.library.db.entity.Game
import com.codebutler.retrograde.lib.retro.RetroDroid
import com.swordfish.touchinput.pads.GamePadFactory
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess
import androidx.fragment.app.Fragment
import com.codebutler.retrograde.R

class GameActivity : RetrogradeActivity() {
    companion object {
        const val EXTRA_GAME_ID = "game_id"
        const val EXTRA_SYSTEM_ID = "system_id"
        const val EXTRA_SAVE_FILE = "save_file"
    }

    @Inject lateinit var gameLoader: GameLoader

    private val progressBar by bindView<ProgressBar>(R.id.progress)
    private val gameDisplayLayout by bindView<FrameLayout>(R.id.game_display_layout)

    private lateinit var gameDisplay: GameDisplay
    private lateinit var gameInput: GameInput

    private lateinit var sharedPreferences: SharedPreferences

    private var game: Game? = null
    private var retroDroid: RetroDroid? = null

    private var dataFragment: DataFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        gameInput = GameInput(this)

        dataFragment = retrieveOrInitializeDataFragment()

        val systemId = intent.getStringExtra(EXTRA_SYSTEM_ID)
        setupTouchInput(systemId)

        val gameId = intent.getIntExtra(EXTRA_GAME_ID, -1)
        gameLoader.load(gameId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scope())
                .subscribe(
                        { data ->
                            progressBar.visibility = View.GONE
                            loadRetro(data, dataFragment?.emulatorState ?: data.saveData)
                            dataFragment?.emulatorState = null
                        },
                        { error ->
                            Timber.e(error, "Failed to load game")
                            finish()
                        })
    }

    override fun onStop() {
        super.onStop()
        val newState = retroDroid?.serialize()
        Timber.d("Storing new fragment state ${newState?.size} into $dataFragment")
        dataFragment?.emulatorState = newState
    }

    private fun retrieveOrInitializeDataFragment(): DataFragment {
        var dataFragment = supportFragmentManager.findFragmentByTag("data") as DataFragment?
        if (dataFragment == null) {
            dataFragment = DataFragment().apply {
                supportFragmentManager.beginTransaction().add(this, "data").commit()
            }
        }
        return dataFragment
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
        val frameLayout = findViewById<FrameLayout>(R.id.game_layout)

        val gamePadLayout = when (systemId) {
            in listOf(GameSystem.GBA_ID) -> GamePadFactory.Layout.GBA
            in listOf(GameSystem.SNES_ID) -> GamePadFactory.Layout.SNES
            in listOf(GameSystem.NES_ID, GameSystem.GB_ID, GameSystem.GBC_ID) -> GamePadFactory.Layout.NES
            in listOf(GameSystem.GENESIS_ID) -> GamePadFactory.Layout.GENESIS
            else -> GamePadFactory.Layout.PSX
        }

        val gameView = gamePadLayout?.let {
            GamePadFactory.getGamePadView(this, it)
        }

        if (gameView != null) {
            frameLayout.addView(gameView)

            gameView.getEvents()
                    .doOnNext {
                        if (it.action == KeyEvent.ACTION_DOWN) {
                            performHapticFeedback(gameView)
                        }
                    }.autoDisposable(scope())
                    .subscribe { gameInput.onKeyEvent(KeyEvent(it.action, it.keycode)) }
        }
    }

    private fun performHapticFeedback(view: View) {
        val flags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, flags)
    }

    override fun onDestroy() {
        super.onDestroy()
        // This activity runs in its own process which should not live beyond the activity lifecycle.
        if (!isChangingConfigurations) {
            exitProcess(0)
        }
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        super.dispatchGenericMotionEvent(event)
        gameInput.onMotionEvent(event)
        return true
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        super.dispatchKeyEvent(event)
        gameInput.onKeyEvent(event)
        return true
    }

    override fun onBackPressed() {
        // We are temporarily doing everything in the UI thread. We will handle this properly using Rx.
        retroDroid?.stop()
        val optionalSaveData = retroDroid?.serialize()
        retroDroid?.unloadGame()
        if (optionalSaveData != null) {
            val tmpFile = createTempFile()
            tmpFile.writeBytes(optionalSaveData)

            val resultData = Intent()
            resultData.putExtra(EXTRA_GAME_ID, game?.id)
            resultData.putExtra(EXTRA_SAVE_FILE, tmpFile.absolutePath)
            setResult(Activity.RESULT_OK, resultData)
            finish()
        } else {
            setResult(Activity.RESULT_CANCELED, null)
            finish()
        }
    }

    private fun loadRetro(data: GameLoader.GameData, state: ByteArray?) {
        try {
            val shaderPreference =
                    sharedPreferences.getBoolean(getString(R.string.pref_key_shader), true)

            gameDisplay = GlGameDisplay(this, RetroShader.build(shaderPreference, data.game.systemId))
            gameDisplayLayout.addView(gameDisplay.view, MATCH_PARENT, MATCH_PARENT)
            lifecycle.addObserver(gameDisplay)

            val retroDroid = RetroDroid(gameDisplay, GameAudio(), gameInput, this, data.coreFile)
            lifecycle.addObserver(retroDroid)

            retroDroid.loadGame(data.gameFile.absolutePath, state)
            retroDroid.start()

            this.game = data.game
            this.retroDroid = retroDroid
        } catch (ex: Exception) {
            Timber.e(ex, "Exception during retro initialization")
            finish()
        }
    }

    class DataFragment : Fragment() {
        var emulatorState: ByteArray? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            retainInstance = true
        }
    }

    @dagger.Module
    class Module
}
