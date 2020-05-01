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

package com.swordfish.lemuroid.app.mobile.feature.game

import android.content.res.Configuration
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.lemuroid.lib.ui.setVisibleOrInvisible
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.touchinput.events.OptionType
import com.swordfish.touchinput.events.PadEvent
import com.swordfish.touchinput.pads.BaseGamePad
import com.swordfish.touchinput.pads.GamePadFactory
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class GameActivity : BaseGameActivity() {

    private var preferenceVibrateOnTouch = true
    private var tiltSensitivity = 0.5f

    private lateinit var virtualGamePad: BaseGamePad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceVibrateOnTouch = settingsManager.vibrateOnTouch
        tiltSensitivity = settingsManager.tiltSensitivity

        setupVirtualPad(system)

        handleOrientationChange(resources.configuration.orientation)
    }

    override fun onResume() {
        super.onResume()
        virtualGamePad.setTiltSensitivity(tiltSensitivity)
    }

    override fun onVariablesRead(coreVariables: List<CoreVariable>) {
        super.onVariablesRead(coreVariables)

        if (system.id == SystemID.PSX) {
            val isPad1Dualshock = coreVariables
                .filter { it.key == "pcsx_rearmed_pad1type" }
                .map { it.value == "dualshock" }
                .firstOrNull() ?: false

            overlayLayout.findViewById<View>(R.id.leftanalog)?.setVisibleOrInvisible(isPad1Dualshock)
            overlayLayout.findViewById<View>(R.id.rightanalog)?.setVisibleOrInvisible(isPad1Dualshock)
        }
    }

    override fun getDialogClass() = GameMenuActivity::class.java

    override fun getShaderForSystem(useShader: Boolean, system: GameSystem): Int {
        if (!useShader) {
            return GLRetroView.SHADER_DEFAULT
        }

        return when (system.id) {
            SystemID.GBA -> GLRetroView.SHADER_LCD
            SystemID.GBC -> GLRetroView.SHADER_LCD
            SystemID.GB -> GLRetroView.SHADER_LCD
            SystemID.N64 -> GLRetroView.SHADER_CRT
            SystemID.GENESIS -> GLRetroView.SHADER_CRT
            SystemID.NES -> GLRetroView.SHADER_CRT
            SystemID.SNES -> GLRetroView.SHADER_CRT
            SystemID.FBNEO -> GLRetroView.SHADER_CRT
            SystemID.SMS -> GLRetroView.SHADER_CRT
            SystemID.PSP -> GLRetroView.SHADER_LCD
            SystemID.NDS -> GLRetroView.SHADER_LCD
            SystemID.GG -> GLRetroView.SHADER_LCD
            SystemID.ATARI2600 -> GLRetroView.SHADER_CRT
            SystemID.PSX -> GLRetroView.SHADER_CRT
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleOrientationChange(newConfig.orientation)
    }

    private fun handleOrientationChange(orientation: Int) {
        OrientationHandler().handleOrientationChange(orientation)
    }

    private fun setupVirtualPad(system: GameSystem) {
        virtualGamePad = GamePadFactory.getGamePadView(this, system)

        overlayLayout.addView(virtualGamePad)
        lifecycle.addObserver(virtualGamePad)

        virtualGamePad.getEvents()
            .subscribeOn(Schedulers.computation())
            .doOnNext {
                if (it.haptic && preferenceVibrateOnTouch) {
                    performHapticFeedback(virtualGamePad)
                }
            }
            .autoDispose(scope())
            .subscribe {
                when (it) {
                    is PadEvent.Option -> handlePadOption(it.optionType)
                    is PadEvent.Button -> retroGameView?.sendKeyEvent(it.action, it.keycode)
                    is PadEvent.Stick -> retroGameView?.sendMotionEvent(it.source, it.xAxis, it.yAxis)
                }
            }

        gamePadManager
            .getGamePadsObservable()
            .map { it.size }
            .autoDispose(scope())
            .subscribeBy(Timber::e) { overlayLayout.setVisibleOrGone(it == 0) }
    }

    private fun performHapticFeedback(view: View) {
        val flags =
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, flags)
    }

    private fun handlePadOption(option: OptionType) {
        when (option) {
            OptionType.SETTINGS -> displayOptionsDialog()
        }
    }

    inner class OrientationHandler {

        fun handleOrientationChange(orientation: Int) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {

                // Finally we should also avoid system bars. Touch element might appear under system bars, or the game
                // view might be cut due to rounded corners.
                setContainerWindowsInsets(top = true, bottom = true)
                changeGameViewConstraints(ConstraintSet.BOTTOM, ConstraintSet.TOP)
            } else {
                changeGameViewConstraints(ConstraintSet.BOTTOM, ConstraintSet.BOTTOM)
                setContainerWindowsInsets(top = false, bottom = true)
            }
        }

        private fun changeGameViewConstraints(gameViewConstraint: Int, padConstraint: Int) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(containerLayout)
            constraintSet.connect(R.id.gameview_layout, gameViewConstraint, R.id.overlay_layout, padConstraint, 0)
            constraintSet.applyTo(containerLayout)
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
}
