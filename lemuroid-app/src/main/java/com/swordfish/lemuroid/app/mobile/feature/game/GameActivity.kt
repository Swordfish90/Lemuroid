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
import android.widget.Toast
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.touchinput.events.PadEvent
import com.swordfish.touchinput.pads.GamePadFactory
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.schedulers.Schedulers

class GameActivity : BaseGameActivity() {

    private var preferenceVibrateOnTouch = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceVibrateOnTouch = settingsManager.vibrateOnTouch

        setupVirtualPad(system)

        handleOrientationChange(resources.configuration.orientation)
    }

    override fun displayToast(id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
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
        val gameView = GamePadFactory.getGamePadView(this, system)

        overlayLayout.addView(gameView)

        gameView.getEvents()
            .subscribeOn(Schedulers.computation())
            .doOnNext {
                if (it.haptic && preferenceVibrateOnTouch) {
                    performHapticFeedback(gameView)
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

        retroGameView?.getConnectedGamepads()
            ?.autoDispose(scope())
            ?.subscribe { overlayLayout.setVisibleOrGone(it == 0) }
    }

    private fun performHapticFeedback(view: View) {
        val flags =
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, flags)
    }
}
