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

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintSet
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.touchinput.radial.VirtualGamePadCustomizer
import com.swordfish.touchinput.radial.VirtualGamePadSettingsManager
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.touchinput.radial.GamePadFactory
import com.swordfish.touchinput.radial.LemuroidVirtualGamePad
import com.swordfish.touchinput.radial.RadialPadConfigs
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import timber.log.Timber

class GameActivity : BaseGameActivity() {

    private var tiltSensitivity = 0.5f

    private lateinit var virtualGamePad: LemuroidVirtualGamePad
    private lateinit var virtualGamePadSettingsManager: VirtualGamePadSettingsManager
    private lateinit var virtualGamePadCustomizer: VirtualGamePadCustomizer
    private var virtualGamePadCustomizationWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tiltSensitivity = settingsManager.tiltSensitivity

        virtualGamePadSettingsManager = VirtualGamePadSettingsManager(applicationContext, system.id)
        virtualGamePadCustomizer = VirtualGamePadCustomizer(virtualGamePadSettingsManager, system)

        setupVirtualPad(system)

        handleOrientationChange(getCurrentOrientation())
    }

    override fun areGamePadsEnabled(): Boolean {
        return settingsManager.gamepadsEnabled
    }

    private fun getCurrentOrientation() = resources.configuration.orientation

    override fun onResume() {
        super.onResume()
        virtualGamePad.tiltSensitivity = tiltSensitivity
    }

    override fun getDialogClass() = GameMenuActivity::class.java

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleOrientationChange(newConfig.orientation)
    }

    private fun handleOrientationChange(orientation: Int) {
        OrientationHandler().handleOrientationChange(orientation)
    }

    private fun setupVirtualPad(system: GameSystem) {
        virtualGamePad = GamePadFactory.createRadialGamePad(this, system.id, settingsManager.vibrateOnTouch)

        overlayLayout.addView(virtualGamePad)

        virtualGamePad.getEvents()
            .autoDispose(scope())
            .subscribe {
                when (it) {
                    is Event.Gesture -> { handleGamePadGesture(it) }
                    is Event.Button -> { handleGamePadButton(it) }
                    is Event.Direction -> { handleGamePadDirection(it) }
                }
            }

        lifecycle.addObserver(virtualGamePad)

        gamePadManager
            .getGamePadsObservable()
            .map { it.size }
            .autoDispose(scope())
            .subscribeBy(Timber::e) {
                val isVisible = !areGamePadsEnabled() || it == 0
                overlayLayout.setVisibleOrGone(isVisible)
            }
    }

    private fun handleGamePadGesture(it: Event.Gesture) {
        if (it.type == GestureType.SINGLE_TAP && it.id == KeyEvent.KEYCODE_BUTTON_MODE) {
            displayOptionsDialog()
        }
    }

    private fun handleGamePadButton(it: Event.Button) {
        retroGameView?.sendKeyEvent(it.action, it.id)
    }

    private fun handleGamePadDirection(it: Event.Direction) {
        when (it.id) {
            RadialPadConfigs.MOTION_SOURCE_DPAD -> {
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_DPAD, it.xAxis, it.yAxis)
            }
            RadialPadConfigs.MOTION_SOURCE_LEFT_STICK -> {
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_LEFT, it.xAxis, it.yAxis)
            }
            RadialPadConfigs.MOTION_SOURCE_RIGHT_STICK -> {
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_RIGHT, it.xAxis, it.yAxis)
            }
            RadialPadConfigs.MOTION_SOURCE_DPAD_AND_LEFT_STICK -> {
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_ANALOG_LEFT, it.xAxis, it.yAxis)
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_DPAD, it.xAxis, it.yAxis)
            }
        }
    }

    override fun onBackPressed() {
        if (virtualGamePadCustomizationWindow?.isShowing == true) {
            virtualGamePadCustomizationWindow?.dismiss()
            virtualGamePadCustomizationWindow = null
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DIALOG_REQUEST) {
            if (data?.getBooleanExtra(GameMenuContract.RESULT_EDIT_TOUCH_CONTROLS, false) == true) {
                if (virtualGamePadCustomizationWindow?.isShowing == true) {
                    return
                }

                if (overlayLayout.visibility != View.VISIBLE) {
                    displayToast(R.string.game_edit_touch_controls_error_not_visible)
                    return
                }

                if (getCurrentOrientation() == Configuration.ORIENTATION_PORTRAIT) {
                    virtualGamePadCustomizer.displayPortraitDialog(this, gameViewLayout, virtualGamePad)
                } else {
                    virtualGamePadCustomizer.displayLandscapeDialog(this, gameViewLayout, virtualGamePad)
                }
            }
        }
    }

    inner class OrientationHandler {

        fun handleOrientationChange(orientation: Int) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(containerLayout)

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {

                // Finally we should also avoid system bars. Touch element might appear under system bars, or the game
                // view might be cut due to rounded corners.
                setContainerWindowsInsets(top = true, bottom = true)

                constraintSet.connect(
                    R.id.gameview_layout,
                    ConstraintSet.BOTTOM,
                    R.id.overlay_layout,
                    ConstraintSet.TOP
                )

                constraintSet.clear(R.id.overlay_layout, ConstraintSet.TOP)

                constraintSet.constrainHeight(R.id.overlay_layout, ConstraintSet.WRAP_CONTENT)

                virtualGamePadCustomizer.loadPortraitSettingsIntoGamePad(virtualGamePad)
            } else {
                setContainerWindowsInsets(top = false, bottom = true)

                constraintSet.connect(
                    R.id.gameview_layout,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )

                constraintSet.connect(
                    R.id.overlay_layout,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )

                constraintSet.constrainHeight(R.id.overlay_layout, ConstraintSet.MATCH_CONSTRAINT)

                virtualGamePadCustomizer.loadLandscapeSettingsIntoGamePad(virtualGamePad)
            }

            virtualGamePad.orientation = orientation

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
