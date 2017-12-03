/*
 * GameActivity.kt
 *
 * Copyright (C) 2017 Odyssey Project
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

package com.codebutler.odyssey.app.feature.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.codebutler.odyssey.BuildConfig
import com.codebutler.odyssey.R
import com.codebutler.odyssey.app.OdysseyConfig
import com.codebutler.odyssey.common.kotlin.bindView
import com.codebutler.odyssey.common.kotlin.isAllZeros
import com.codebutler.odyssey.lib.android.OdysseyActivity
import com.codebutler.odyssey.lib.core.CoreManager
import com.codebutler.odyssey.lib.game.GameLoader
import com.codebutler.odyssey.lib.game.audio.GameAudio
import com.codebutler.odyssey.lib.game.display.GameDisplay
import com.codebutler.odyssey.lib.game.display.gl.GlGameDisplay
import com.codebutler.odyssey.lib.game.display.sw.SwGameDisplay
import com.codebutler.odyssey.lib.library.GameLibrary
import com.codebutler.odyssey.lib.library.db.OdysseyDatabase
import com.codebutler.odyssey.lib.library.db.entity.Game
import com.codebutler.odyssey.lib.retro.RetroDroid
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposeWith
import dagger.Provides
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class GameActivity : OdysseyActivity() {
    companion object {
        private const val EXTRA_GAME_ID = "game_id"

        fun newIntent(context: Context, game: Game)
                = Intent(context, GameActivity::class.java).apply {
            putExtra(EXTRA_GAME_ID, game.id)
        }
    }

    @Inject lateinit var gameLibrary: GameLibrary
    @Inject lateinit var gameLoader: GameLoader

    private val progressBar by bindView<ProgressBar>(R.id.progress)
    private val gameDisplayLayout by bindView<FrameLayout>(R.id.game_display_layout)

    private lateinit var gameDisplay: GameDisplay

    private var game: Game? = null
    private var retroDroid: RetroDroid? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        if (OdysseyConfig.ENABLE_GLSURFACEVIEW) {
            gameDisplay = GlGameDisplay(this)
        } else {
            gameDisplay = SwGameDisplay(this)
        }

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
                .autoDisposeWith(AndroidLifecycleScopeProvider.from(this@GameActivity))
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

    override fun onDestroy() {
        super.onDestroy()
        // This activity runs in its own process which should not live beyond the activity lifecycle.
        System.exit(0)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        super.dispatchGenericMotionEvent(event)
        retroDroid?.onMotionEvent(event)
        return true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        super.dispatchKeyEvent(event)
        retroDroid?.onKeyEvent(event)
        return true
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
            val retroDroid = RetroDroid(gameDisplay, GameAudio(), this, data.coreFile)
            lifecycle.addObserver(retroDroid)

            retroDroid.gameUnloadedCallback = { saveData ->
                val game = this.game
                val saveCompletable = if (saveData != null && saveData.isAllZeros().not() && game != null) {
                    gameLibrary.setGameSave(game, saveData)
                } else {
                    Completable.complete()
                }
                saveCompletable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
            }

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
    class Module {

        @Provides
        fun gameLoader(coreManager: CoreManager, odysseyDatabase: OdysseyDatabase, gameLibrary: GameLibrary)
                = GameLoader(coreManager, odysseyDatabase, gameLibrary)
    }
}
