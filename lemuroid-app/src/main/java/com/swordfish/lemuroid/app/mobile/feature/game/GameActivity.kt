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

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import com.gojuno.koptional.Some
import com.gojuno.koptional.toOptional
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.common.graphics.GraphicsUtils
import com.swordfish.lemuroid.common.math.linearInterpolation
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.radialgamepad.library.RadialGamePad
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.touchinput.radial.RadialPadConfigs
import com.swordfish.lemuroid.lib.controller.TouchControllerCustomizer
import com.swordfish.lemuroid.lib.controller.TouchControllerSettingsManager
import com.swordfish.touchinput.radial.sensors.TiltSensor
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class GameActivity : BaseGameActivity() {
    private lateinit var tiltSensor: TiltSensor
    private var currentTiltId: Int? = null
    private val tiltTrackedIds = setOf(
        RadialPadConfigs.MOTION_SOURCE_LEFT_STICK,
        RadialPadConfigs.MOTION_SOURCE_RIGHT_STICK
    )

    private var leftPad: RadialGamePad? = null
    private var rightPad: RadialGamePad? = null

    private val virtualControllerDisposables = CompositeDisposable()

    private var controllerConfig: ControllerConfig? = null

    var padSettings: TouchControllerSettingsManager.Settings by Delegates.observable(
        TouchControllerSettingsManager.Settings()
    ) { _, _, _ ->
        updateLayout()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tiltSensor = TiltSensor(applicationContext)
        tiltSensor.setSensitivity(settingsManager.tiltSensitivity)

        setupVirtualGamePadVisibility()
        setVirtualGamePads()
    }

    private fun setVirtualGamePads() {
        getControllerType()
            .map { it[0].toOptional() }
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                when (it) {
                    is Some -> {
                        initializeController(it.value)
                    }
                    else -> { /*Do nothing*/ }
                }
            }
            .autoDispose(scope())
            .subscribeBy(Timber::e) { }
    }

    private fun setupVirtualGamePadVisibility() {
        gamePadManager
            .getGamePadsObservable()
            .map { it.size }
            .autoDispose(scope())
            .subscribeBy(Timber::e) {
                val isVisible = !areGamePadsEnabled() || it == 0
                leftGamePadContainer.setVisibleOrGone(isVisible)
                rightGamePadContainer.setVisibleOrGone(isVisible)
            }
    }

    override fun areGamePadsEnabled(): Boolean {
        return settingsManager.gamepadsEnabled
    }

    private fun getCurrentOrientation() = resources.configuration.orientation

    override fun getDialogClass() = GameMenuActivity::class.java

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        loadVirtualGamePadSettings()
        updateLayout()
    }

    private fun updateLayout() {
        controllerConfig?.let { LayoutHandler().handleOrientationChange(it) }
    }

    private fun initializeController(controllerConfig: ControllerConfig) {
        this.controllerConfig = controllerConfig

        virtualControllerDisposables.clear()
        leftGamePadContainer.removeAllViews()
        rightGamePadContainer.removeAllViews()

        val touchControllerConfig = controllerConfig.getTouchControllerConfig()

        val leftPad = RadialGamePad(
            wrapGamePadConfig(
                applicationContext,
                touchControllerConfig.leftConfig,
                settingsManager.vibrateOnTouch
            ),
            DEFAULT_MARGINS_DP,
            this
        )
        leftGamePadContainer.addView(leftPad)

        val rightPad = RadialGamePad(
            wrapGamePadConfig(
                applicationContext,
                touchControllerConfig.rightConfig,
                settingsManager.vibrateOnTouch
            ),
            DEFAULT_MARGINS_DP,
            this
        )
        rightGamePadContainer.addView(rightPad)

        loadVirtualGamePadSettings()

        virtualControllerDisposables.add(
            Observable.merge(leftPad.events(), rightPad.events())
                .doOnNext { handleTrackingEvent(it) }
                .subscribeBy {
                    when (it) {
                        is Event.Button -> {
                            handleGamePadButton(it)
                        }
                        is Event.Direction -> {
                            handleGamePadDirection(it)
                        }
                    }
                }
        )

        this.leftPad = leftPad
        this.rightPad = rightPad

        updateLayout()
    }

    override fun onDestroy() {
        super.onDestroy()
        virtualControllerDisposables.clear()
    }

    private fun handleTrackingEvent(it: Event?) {
        when (it) {
            is Event.Gesture -> {
                if (it.type == GestureType.TRIPLE_TAP && it.id in tiltTrackedIds) {
                    startTrackingId(it.id)
                } else if (it.id == currentTiltId) {
                    stopTrackingId(it.id)
                }
            }
        }
    }

    private fun getGamePadTheme(context: Context): RadialGamePadTheme {
        val accentColor = GraphicsUtils.colorToRgb(context.getColor(R.color.colorPrimary))
        val alpha = (255 * PRESSED_COLOR_ALPHA).roundToInt()
        val pressedColor = GraphicsUtils.rgbaToColor(accentColor + listOf(alpha))
        return RadialGamePadTheme(
            normalColor = context.getColor(R.color.touch_control_normal),
            pressedColor = pressedColor,
            primaryDialBackground = context.getColor(R.color.touch_control_background),
            textColor = context.getColor(R.color.touch_control_text)
        )
    }

    private fun wrapGamePadConfig(
        context: Context,
        config: RadialGamePadConfig,
        vibrateOnTouch: Boolean
    ): RadialGamePadConfig {
        val padTheme = getGamePadTheme(context)
        return config.copy(theme = padTheme, haptic = vibrateOnTouch)
    }

    private fun handleGamePadButton(it: Event.Button) {
        if (it.action == KeyEvent.ACTION_DOWN && it.id == KeyEvent.KEYCODE_BUTTON_MODE) {
            displayOptionsDialog()
        } else {
            retroGameView?.sendKeyEvent(it.action, it.id)
        }
    }

    private fun handleGamePadDirection(it: Event.Direction) {
        when (it.id) {
            RadialPadConfigs.MOTION_SOURCE_DPAD -> {
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_DPAD, it.xAxis, it.yAxis)
            }
            RadialPadConfigs.MOTION_SOURCE_LEFT_STICK -> {
                retroGameView?.sendMotionEvent(
                    GLRetroView.MOTION_SOURCE_ANALOG_LEFT,
                    it.xAxis,
                    it.yAxis
                )
            }
            RadialPadConfigs.MOTION_SOURCE_RIGHT_STICK -> {
                retroGameView?.sendMotionEvent(
                    GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,
                    it.xAxis,
                    it.yAxis
                )
            }
            RadialPadConfigs.MOTION_SOURCE_DPAD_AND_LEFT_STICK -> {
                retroGameView?.sendMotionEvent(
                    GLRetroView.MOTION_SOURCE_ANALOG_LEFT,
                    it.xAxis,
                    it.yAxis
                )
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_DPAD, it.xAxis, it.yAxis)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DIALOG_REQUEST) {
            if (data?.getBooleanExtra(GameMenuContract.RESULT_EDIT_TOUCH_CONTROLS, false) == true) {
                displayCustomizationOptions()
                    .autoDispose(scope())
                    .subscribe()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        tiltSensor.isAllowedToRun = false
    }

    override fun onResume() {
        super.onResume()

        tiltSensor
            .getTiltEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { sendTiltEvent(it) }

        tiltSensor.isAllowedToRun = true
    }

    private fun sendTiltEvent(sensorValues: FloatArray) {
        currentTiltId?.let {
            val xTilt = (sensorValues[0] + 1f) / 2f
            val yTilt = (sensorValues[1] + 1f) / 2f
            rightPad?.simulateMotionEvent(it, xTilt, yTilt)
            leftPad?.simulateMotionEvent(it, xTilt, yTilt)
        }
    }

    private fun stopTrackingId(id: Int) {
        currentTiltId = null
        tiltSensor.shouldRun = false
        leftPad?.simulateClearMotionEvent(id)
        rightPad?.simulateClearMotionEvent(id)
    }

    private fun startTrackingId(id: Int) {
        if (currentTiltId != id) {
            currentTiltId?.let { stopTrackingId(it) }
            currentTiltId = id
            tiltSensor.shouldRun = true
        }
    }

    private fun loadVirtualGamePadSettings() {
        val virtualGamePadSettingsManager = getVirtualGamePadSettingsManager() ?: return
        padSettings = virtualGamePadSettingsManager.retrieveSettings()
    }

    private fun getVirtualGamePadSettingsManager(): TouchControllerSettingsManager? {
        val orientation = if (getCurrentOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            TouchControllerSettingsManager.Orientation.PORTRAIT
        } else {
            TouchControllerSettingsManager.Orientation.LANDSCAPE
        }

        return controllerConfig?.let {
            TouchControllerSettingsManager(applicationContext, it.touchControllerID, orientation)
        }
    }

    private fun storeVirtualGamePadSettings() {
        val virtualGamePadSettingsManager = getVirtualGamePadSettingsManager() ?: return
        virtualGamePadSettingsManager.storeSettings(padSettings)
    }

    private fun displayCustomizationOptions(): Completable {
        val customizer = TouchControllerCustomizer()
        val initialSettings = TouchControllerCustomizer.Settings(
            padSettings.scale,
            padSettings.rotation,
            padSettings.marginX,
            padSettings.marginY
        )

        val customizeObservable = customizer.displayCustomizationPopup(
            this@GameActivity,
            layoutInflater,
            mainContainerLayout,
            initialSettings
        )

        return customizeObservable
            .doOnNext {
                when (it) {
                    is TouchControllerCustomizer.Event.Scale -> {
                        padSettings = padSettings.copy(scale = it.value)
                    }
                    is TouchControllerCustomizer.Event.Rotation -> {
                        padSettings = padSettings.copy(rotation = it.value)
                    }
                    is TouchControllerCustomizer.Event.Margins -> {
                        padSettings = padSettings.copy(marginX = it.x, marginY = it.y)
                    }
                    else -> Unit
                }
            }
            .doOnSubscribe { findViewById<View>(R.id.editcontrolsdarkening).setVisibleOrGone(true) }
            .doFinally { findViewById<View>(R.id.editcontrolsdarkening).setVisibleOrGone(false) }
            .doFinally { storeVirtualGamePadSettings() }
            .ignoreElements()
    }

    inner class LayoutHandler {

        private fun handleRetroViewLayout(constraintSet: ConstraintSet, controllerConfig: ControllerConfig) {
            if (this@GameActivity.getCurrentOrientation() == Configuration.ORIENTATION_PORTRAIT) {
                constraintSet.connect(
                    R.id.gamecontainer,
                    ConstraintSet.BOTTOM,
                    R.id.leftgamepad,
                    ConstraintSet.TOP
                )

                constraintSet.connect(
                    R.id.gamecontainer,
                    ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT
                )

                constraintSet.connect(
                    R.id.gamecontainer,
                    ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT
                )

                constraintSet.connect(
                    R.id.gamecontainer,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
            } else {
                constraintSet.connect(
                    R.id.gamecontainer,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )

                constraintSet.connect(
                    R.id.gamecontainer,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )

                if (controllerConfig.allowTouchOverlay) {
                    constraintSet.connect(
                        R.id.gamecontainer,
                        ConstraintSet.LEFT,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.LEFT
                    )

                    constraintSet.connect(
                        R.id.gamecontainer,
                        ConstraintSet.RIGHT,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.RIGHT
                    )
                } else {
                    constraintSet.connect(
                        R.id.gamecontainer,
                        ConstraintSet.LEFT,
                        R.id.leftgamepad,
                        ConstraintSet.RIGHT
                    )

                    constraintSet.connect(
                        R.id.gamecontainer,
                        ConstraintSet.RIGHT,
                        R.id.rightgamepad,
                        ConstraintSet.LEFT
                    )
                }
            }

            constraintSet.constrainedWidth(R.id.gamecontainer, true)
            constraintSet.constrainedHeight(R.id.gamecontainer, true)
        }

        private fun handleVirtualGamePadLayout(
            constraintSet: ConstraintSet,
            controllerConfig: ControllerConfig
        ) {
            val orientation = this@GameActivity.getCurrentOrientation()
            val touchControllerConfig = controllerConfig.getTouchControllerConfig()

            val leftPad = leftPad ?: return
            val rightPad = rightPad ?: return

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                constraintSet.clear(R.id.leftgamepad, ConstraintSet.TOP)
                constraintSet.clear(R.id.rightgamepad, ConstraintSet.TOP)
            } else {
                constraintSet.connect(
                    R.id.leftgamepad,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    R.id.rightgamepad,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
            }

            val minScale = TouchControllerSettingsManager.MIN_SCALE
            val maxScale = TouchControllerSettingsManager.MAX_SCALE

            val leftScale = linearInterpolation(
                padSettings.scale,
                minScale,
                maxScale
            ) * touchControllerConfig.leftScale

            val rightScale = linearInterpolation(
                padSettings.scale,
                minScale,
                maxScale
            ) * touchControllerConfig.rightScale

            val maxMargins = GraphicsUtils.convertDpToPixel(
                TouchControllerSettingsManager.MAX_MARGINS,
                applicationContext
            )

            constraintSet.setHorizontalWeight(R.id.leftgamepad, touchControllerConfig.leftScale)
            constraintSet.setHorizontalWeight(R.id.rightgamepad, touchControllerConfig.rightScale)

            leftPad.primaryDialMaxSizeDp = DEFAULT_PRIMARY_DIAL_SIZE * leftScale
            rightPad.primaryDialMaxSizeDp = DEFAULT_PRIMARY_DIAL_SIZE * rightScale

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                leftPad.spacingBottom = linearInterpolation(
                    padSettings.marginY,
                    0f,
                    maxMargins
                ).roundToInt()
                leftPad.spacingLeft = 0
                rightPad.spacingBottom = linearInterpolation(
                    padSettings.marginY,
                    0f,
                    maxMargins
                ).roundToInt()
                rightPad.spacingRight = 0

                leftPad.offsetX = linearInterpolation(padSettings.marginX, 0f, maxMargins)
                rightPad.offsetX = -linearInterpolation(padSettings.marginX, 0f, maxMargins)

                leftPad.offsetY = 0f
                rightPad.offsetY = 0f
            } else {
                leftPad.spacingBottom = 0
                leftPad.spacingLeft = linearInterpolation(padSettings.marginX, 0f, maxMargins).roundToInt()
                rightPad.spacingBottom = 0
                rightPad.spacingRight = linearInterpolation(
                    padSettings.marginX,
                    0f,
                    maxMargins
                ).roundToInt()

                leftPad.offsetX = 0f
                rightPad.offsetX = 0f

                leftPad.offsetY = -linearInterpolation(padSettings.marginY, 0f, maxMargins)
                rightPad.offsetY = -linearInterpolation(padSettings.marginY, 0f, maxMargins)
            }

            leftPad.gravityY = 1f
            rightPad.gravityY = 1f

            leftPad.gravityX = -1f
            rightPad.gravityX = 1f

            val constrainHeight = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                ConstraintSet.WRAP_CONTENT
            } else {
                ConstraintSet.MATCH_CONSTRAINT
            }

            val constrainWidth = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                ConstraintSet.MATCH_CONSTRAINT
            } else {
                ConstraintSet.WRAP_CONTENT
            }

            constraintSet.constrainHeight(R.id.leftgamepad, constrainHeight)
            constraintSet.constrainHeight(R.id.rightgamepad, constrainHeight)
            constraintSet.constrainWidth(R.id.leftgamepad, constrainWidth)
            constraintSet.constrainWidth(R.id.rightgamepad, constrainWidth)

            if (controllerConfig.allowTouchRotation) {
                val maxRotation = TouchControllerSettingsManager.MAX_ROTATION
                leftPad.secondaryDialRotation = linearInterpolation(padSettings.rotation, 0f, maxRotation)
                rightPad.secondaryDialRotation = -linearInterpolation(padSettings.rotation, 0f, maxRotation)
            }
        }

        fun handleOrientationChange(config: ControllerConfig) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainContainerLayout)

            handleVirtualGamePadLayout(constraintSet, config)
            handleRetroViewLayout(constraintSet, config)

            constraintSet.applyTo(mainContainerLayout)

            mainContainerLayout.requestLayout()
            mainContainerLayout.invalidate()
        }
    }

    companion object {
        const val DEFAULT_MARGINS_DP = 8f

        const val PRESSED_COLOR_ALPHA = 0.5f

        const val DEFAULT_PRIMARY_DIAL_SIZE = 160f
    }
}
