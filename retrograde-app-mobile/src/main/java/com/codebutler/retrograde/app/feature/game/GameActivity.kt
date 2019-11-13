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

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import com.codebutler.retrograde.common.kotlin.bindView
import com.codebutler.retrograde.lib.android.RetrogradeActivity
import com.codebutler.retrograde.lib.library.GameSystem
import com.swordfish.touchinput.pads.GamePadFactory
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import timber.log.Timber
import kotlin.system.exitProcess
import androidx.fragment.app.Fragment
import com.codebutler.retrograde.R
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.touchinput.events.PadEvent
import io.reactivex.schedulers.Schedulers

class GameActivity : RetrogradeActivity() {
    companion object {
        const val EXTRA_GAME_ID = "game_id"
        const val EXTRA_SYSTEM_ID = "system_id"
        const val EXTRA_SAVE_FILE = "save_file"
        const val EXTRA_CORE_PATH = "core_path"
        const val EXTRA_GAME_PATH = "game_path"
    }

    private val gameLayout by bindView<FrameLayout> (R.id.game_layout)

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var retroGameView: GLRetroView

    private var dataFragment: DataFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        dataFragment = retrieveOrInitializeDataFragment()

        retroGameView = GLRetroView(this)
        retroGameView.onCreate(intent.getStringExtra(EXTRA_CORE_PATH), intent.getStringExtra(EXTRA_GAME_PATH))

        (getCurrentState() ?: getSave())?.let {
            retroGameView.unserialize(it)
        }

        gameLayout.addView(retroGameView)

        val systemId = intent.getStringExtra(EXTRA_SYSTEM_ID)
        setupTouchInput(systemId)
    }

    private fun getCurrentState() = dataFragment?.emulatorState

    private fun getSave() = intent.getByteArrayExtra(EXTRA_SAVE_FILE)

    override fun onResume() {
        super.onResume()
        retroGameView.onResume()
    }

    override fun onPause() {
        retroGameView.onPause()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        val newState = retroGameView.serialize()
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

        val gameView = gamePadLayout.let {
            GamePadFactory.getGamePadView(this, it)
        }

        frameLayout.addView(gameView)

        gameView.getEvents()
                .subscribeOn(Schedulers.computation())
                .doOnNext {
                    // TODO FILIPPO... DPAD Should perform haptic feedback...
                    if (it is PadEvent.Button && it.action == KeyEvent.ACTION_DOWN) {
                        performHapticFeedback(gameView)
                    }
                }.autoDisposable(scope())
                .subscribe {
                    when (it) {
                        is PadEvent.Button -> retroGameView.sendKeyEvent(it.action, it.keycode)
                        is PadEvent.Stick -> retroGameView.sendMotionEvent(it.source, it.xAxis, it.yAxis)
                    }
                }
    }

    private fun performHapticFeedback(view: View) {
        val flags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, flags)
    }

    override fun onDestroy() {
        retroGameView.onDestroy()
        super.onDestroy()
        // This activity runs in its own process which should not live beyond the activity lifecycle.
        if (!isChangingConfigurations) {
            exitProcess(0)
        }
    }

/*    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        super.dispatchGenericMotionEvent(event)
        gameInput.onMotionEvent(event)
        return true
    }*/

/*    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        super.dispatchKeyEvent(event)
        gameInput.onKeyEvent(event)
        return true
    }*/

/*    override fun onBackPressed() {
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
    }*/

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
