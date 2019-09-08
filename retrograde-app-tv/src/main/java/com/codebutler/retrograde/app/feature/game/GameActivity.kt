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
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.codebutler.retrograde.BuildConfig
import com.codebutler.retrograde.R
import com.codebutler.retrograde.common.kotlin.bindView
import com.codebutler.retrograde.lib.android.RetrogradeActivity
import com.codebutler.retrograde.lib.game.GameLoader
import com.codebutler.retrograde.lib.game.audio.GameAudio
import com.codebutler.retrograde.lib.game.display.GameDisplay
import com.codebutler.retrograde.lib.game.display.gl.GlGameDisplay
import com.codebutler.retrograde.lib.game.display.sw.SwGameDisplay
import com.codebutler.retrograde.lib.game.input.GameInput
import com.codebutler.retrograde.lib.library.GameLibrary
import com.codebutler.retrograde.lib.library.db.entity.Game
import com.codebutler.retrograde.lib.retro.RetroDroid
import com.codebutler.retrograde.lib.util.subscribeBy
import com.gojuno.koptional.None
import com.gojuno.koptional.Some
import com.gojuno.koptional.toOptional
import com.swordfish.touchinput.pads.GamePadFactory
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class GameActivity : RetrogradeActivity() {
    companion object {
        const val EXTRA_GAME_ID = "game_id"
        const val EXTRA_SAVE_FILE = "save_file"
    }

    @Inject lateinit var gameLibrary: GameLibrary
    @Inject lateinit var gameLoader: GameLoader

    private val progressBar by bindView<ProgressBar>(R.id.progress)
    private val gameDisplayLayout by bindView<FrameLayout>(R.id.game_display_layout)

    private lateinit var gameDisplay: GameDisplay
    private lateinit var gameInput: GameInput

    private var game: Game? = null
    private var retroDroid: RetroDroid? = null

    private var displayTouchInput: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val enableOpengl = prefs.getBoolean(getString(R.string.pref_key_flags_opengl), false)
        displayTouchInput = prefs.getBoolean(getString(R.string.pref_key_flags_touchinput), false)

        gameDisplay = if (enableOpengl) {
            GlGameDisplay(this)
        } else {
            SwGameDisplay(this)
        }

        gameInput = GameInput(this)

        gameDisplayLayout.addView(gameDisplay.view, MATCH_PARENT, MATCH_PARENT)
        lifecycle.addObserver(gameDisplay)

        // FIXME: Full Activity lifecycle handling.
        if (savedInstanceState != null) {
            return
        }

        val gameId = intent.getIntExtra(EXTRA_GAME_ID, -1)
        gameLoader.load(gameId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scope())
                .subscribe(
                        { data ->
                            progressBar.visibility = View.GONE
                            loadRetro(data)
                        },
                        { error ->
                            Timber.e(error, "Failed to load game")
                            finish()
                        })

        if (BuildConfig.DEBUG) {
            addFpsView()
        }
    }

    private fun setupTouchInput(game: Game) {
        val frameLayout = findViewById<FrameLayout>(R.id.game_layout)

        val gameView = when (game.systemId) {
            in listOf("snes", "gba") -> GamePadFactory.getGamePadView(this, GamePadFactory.Layout.SNES)
            in listOf("nes", "gb", "gbc") -> GamePadFactory.getGamePadView(this, GamePadFactory.Layout.NES)
            in listOf("md") -> GamePadFactory.getGamePadView(this, GamePadFactory.Layout.GENESIS)
            else -> null
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
        val flags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, flags)
    }

    override fun onDestroy() {
        super.onDestroy()
        // This activity runs in its own process which should not live beyond the activity lifecycle.
        System.exit(0)
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
        retroDroid?.stop()
        retroDroid?.unloadGame()
    }

    private fun addFpsView() {
        val frameLayout = findViewById<FrameLayout>(R.id.game_layout)

        val fpsView = TextView(this)
        fpsView.textSize = 18f
        fpsView.setTextColor(Color.WHITE)
        fpsView.setShadowLayer(2f, 0f, 0f, Color.BLACK)

        frameLayout.addView(fpsView)

        fun updateFps() {
            fpsView.text = getString(R.string.fps_format, gameDisplay.fps, retroDroid?.fps ?: 0L)
            fpsView.postDelayed({ updateFps() }, 1000)
        }
        updateFps()
    }

    private fun loadRetro(data: GameLoader.GameData) {
        try {
            val retroDroid = RetroDroid(gameDisplay, GameAudio(), gameInput, this, data.coreFile)
            lifecycle.addObserver(retroDroid)

            if (displayTouchInput) {
                setupTouchInput(data.game)
            }

            retroDroid.gameUnloaded
                .map { optionalSaveData ->
                    if (optionalSaveData is Some) {
                        val tmpFile = createTempFile()
                        tmpFile.writeBytes(optionalSaveData.value)
                        tmpFile.toOptional()
                    } else {
                        None
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scope())
                .subscribeBy(
                    onNext = { optionalTmpFile ->
                        val resultData = Intent()
                        if (optionalTmpFile is Some) {
                            resultData.putExtra(EXTRA_GAME_ID, data.game.id)
                            resultData.putExtra(EXTRA_SAVE_FILE, optionalTmpFile.value.absolutePath)
                        }
                        setResult(Activity.RESULT_OK, resultData)
                        finish()
                    },
                    onError = { error ->
                        Timber.e(error, "Error unloading game")
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )

            retroDroid.loadGame(data.gameFile.absolutePath, data.saveData)
            retroDroid.start()

            this.game = data.game
            this.retroDroid = retroDroid
        } catch (ex: Exception) {
            Timber.e(ex, "Exception during retro initialization")
            finish()
        }
    }

    @dagger.Module
    class Module
}
