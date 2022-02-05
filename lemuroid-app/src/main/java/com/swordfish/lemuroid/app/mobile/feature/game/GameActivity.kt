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
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.rxjava2.filterSome
import com.gojuno.koptional.toOptional
import com.jakewharton.rxrelay2.BehaviorRelay
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.lemuroid.app.mobile.feature.tilt.CrossTiltTracker
import com.swordfish.lemuroid.app.mobile.feature.tilt.StickTiltTracker
import com.swordfish.lemuroid.app.mobile.feature.tilt.TiltTracker
import com.swordfish.lemuroid.app.mobile.feature.tilt.TwoButtonsTiltTracker
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.common.graphics.GraphicsUtils
import com.swordfish.lemuroid.common.math.linearInterpolation
import com.swordfish.lemuroid.common.rx.BehaviorRelayNullableProperty
import com.swordfish.lemuroid.common.rx.BehaviorRelayProperty
import com.swordfish.lemuroid.common.rx.RXUtils
import com.swordfish.lemuroid.common.view.setVisibleOrGone
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.controller.TouchControllerCustomizer
import com.swordfish.lemuroid.lib.controller.TouchControllerSettingsManager
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.radialgamepad.library.RadialGamePad
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.HapticConfig
import com.swordfish.touchinput.radial.LemuroidTouchConfigs
import com.swordfish.touchinput.radial.sensors.TiltSensor
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

class GameActivity : BaseGameActivity() {
    @Inject lateinit var sharedPreferences: Lazy<SharedPreferences>

    private var serviceController: GameService.GameServiceController? = null

    private lateinit var tiltSensor: TiltSensor
    private var currentTiltTracker: TiltTracker? = null

    private var leftPad: RadialGamePad? = null
    private var rightPad: RadialGamePad? = null

    private val virtualControllerDisposables = CompositeDisposable()

    private val touchControllerConfigObservable = BehaviorRelay.createDefault<Optional<ControllerConfig>>(None)
    private var touchControllerConfig: ControllerConfig?
    by BehaviorRelayNullableProperty(touchControllerConfigObservable)

    private val padSettingsObservable = BehaviorRelay.createDefault(TouchControllerSettingsManager.Settings())
    private var padSettings: TouchControllerSettingsManager.Settings
    by BehaviorRelayProperty(padSettingsObservable)

    private val orientationObservable = BehaviorRelay.createDefault(Configuration.ORIENTATION_PORTRAIT)
    private var orientation: Int by BehaviorRelayProperty(orientationObservable)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orientation = getCurrentOrientation()

        tiltSensor = TiltSensor(applicationContext)

        startGameService()

        setupVirtualGamePadVisibility()
        setupVirtualGamePads()

        RXUtils.combineLatest(
            touchControllerConfigObservable.filterSome(),
            orientationObservable,
            isVirtualGamePadVisible(),
            padSettingsObservable
        )
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribeBy(Timber::e) { (config, orientation, virtualGamePadVisible, padSettings) ->
                LayoutHandler().updateLayout(config, padSettings, orientation, virtualGamePadVisible)
            }
    }

    private fun setupVirtualGamePads() {
        val firstGamePad = getControllerType()
            .map { it.get(0).toOptional() }
            .filterSome()
            .distinctUntilChanged()

        Observables.combineLatest(firstGamePad, orientationObservable)
            .flatMapCompletable { (pad, orientation) -> setupController(pad, orientation) }
            .autoDispose(scope())
            .subscribeBy(Timber::e) { }
    }

    private fun setupController(controllerConfig: ControllerConfig, orientation: Int): Completable {
        return settingsManager.hapticFeedbackMode
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { setupTouchViews(controllerConfig, it) }
            .ignoreElement()
            .observeOn(AndroidSchedulers.mainThread())
            .andThen(loadVirtualGamePadSettings(controllerConfig, orientation))
    }

    private fun setupVirtualGamePadVisibility() {
        isVirtualGamePadVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribeBy(Timber::e) {
                leftGamePadContainer.setVisibleOrGone(it)
                rightGamePadContainer.setVisibleOrGone(it)
            }
    }

    private fun isVirtualGamePadVisible(): Observable<Boolean> {
        return inputDeviceManager
            .getEnabledInputsObservable()
            .map { it.isEmpty() }
    }

    private fun getCurrentOrientation() = resources.configuration.orientation

    override fun getDialogClass() = GameMenuActivity::class.java

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientation = newConfig.orientation
    }

    private fun setupTouchViews(controllerConfig: ControllerConfig, hapticFeedbackType: String) {
        virtualControllerDisposables.clear()
        leftGamePadContainer.removeAllViews()
        rightGamePadContainer.removeAllViews()

        val touchControllerConfig = controllerConfig.getTouchControllerConfig()

        val hapticConfig = when (hapticFeedbackType) {
            "none" -> HapticConfig.OFF
            "press" -> HapticConfig.PRESS
            "press_release" -> HapticConfig.PRESS_AND_RELEASE
            else -> HapticConfig.OFF
        }

        val leftConfig = LemuroidTouchConfigs.getRadialGamePadConfig(
            touchControllerConfig.leftConfig,
            applicationContext.getColor(R.color.colorPrimary),
            hapticConfig,
            applicationContext
        )
        val leftPad = RadialGamePad(leftConfig, DEFAULT_MARGINS_DP, this)
        leftGamePadContainer.addView(leftPad)

        val rightConfig = LemuroidTouchConfigs.getRadialGamePadConfig(
            touchControllerConfig.rightConfig,
            applicationContext.getColor(R.color.colorPrimary),
            hapticConfig,
            applicationContext
        )
        val rightPad = RadialGamePad(rightConfig, DEFAULT_MARGINS_DP, this)
        rightGamePadContainer.addView(rightPad)

        val virtualPadEvents = Observable.merge(leftPad.events(), rightPad.events())
            .share()

        setupDefaultActions(virtualPadEvents)
        setupTiltActions(virtualPadEvents)
        setupVirtualMenuActions(virtualPadEvents)

        this.leftPad = leftPad
        this.rightPad = rightPad

        this.touchControllerConfig = controllerConfig
    }

    private fun setupDefaultActions(virtualPadEvents: Observable<Event>) {
        virtualControllerDisposables.add(
            virtualPadEvents
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
    }

    private fun setupTiltActions(virtualPadEvents: Observable<Event>) {
        virtualControllerDisposables.add(
            virtualPadEvents
                .ofType(Event.Gesture::class.java)
                .subscribeOn(Schedulers.single())
                .filter { it.type == GestureType.TRIPLE_TAP }
                .buffer(500, TimeUnit.MILLISECONDS)
                .filter { it.isNotEmpty() }
                .subscribeBy { events ->
                    handleTripleTaps(events)
                }
        )

        virtualControllerDisposables.add(
            virtualPadEvents
                .ofType(Event.Gesture::class.java)
                .filter { it.type == GestureType.FIRST_TOUCH }
                .subscribeOn(Schedulers.single())
                .subscribeBy { event ->
                    currentTiltTracker?.let { tracker ->
                        if (event.id in tracker.trackedIds()) {
                            stopTrackingId(tracker)
                        }
                    }
                }
        )
    }

    private fun setupVirtualMenuActions(virtualPadEvents: Observable<Event>) {
        val allMenuButtonEvents = virtualPadEvents
            .ofType(Event.Button::class.java)
            .filter { it.id == KeyEvent.KEYCODE_BUTTON_MODE }
            .share()

        val cancelMenuButtonEvents = allMenuButtonEvents
            .filter { it.action == KeyEvent.ACTION_UP }
            .map { Unit }

        virtualControllerDisposables.add(
            allMenuButtonEvents
                .filter { it.action == KeyEvent.ACTION_DOWN }
                .concatMapMaybe {
                    VirtualLongPressHandler.displayLoading(
                        this,
                        R.drawable.ic_menu,
                        cancelMenuButtonEvents
                    )
                        .doOnSuccess {
                            displayOptionsDialog()
                            simulateVirtualGamepadHaptic()
                        }
                }
                .subscribeBy(Timber::e) { }
        )
    }

    private fun handleTripleTaps(events: MutableList<Event.Gesture>) {
        val eventsTracker = when (events.map { it.id }.toSet()) {
            setOf(LemuroidTouchConfigs.MOTION_SOURCE_LEFT_STICK) -> StickTiltTracker(
                LemuroidTouchConfigs.MOTION_SOURCE_LEFT_STICK
            )
            setOf(LemuroidTouchConfigs.MOTION_SOURCE_RIGHT_STICK) -> StickTiltTracker(
                LemuroidTouchConfigs.MOTION_SOURCE_RIGHT_STICK
            )
            setOf(LemuroidTouchConfigs.MOTION_SOURCE_DPAD) -> CrossTiltTracker(
                LemuroidTouchConfigs.MOTION_SOURCE_DPAD
            )
            setOf(LemuroidTouchConfigs.MOTION_SOURCE_DPAD_AND_LEFT_STICK) -> CrossTiltTracker(
                LemuroidTouchConfigs.MOTION_SOURCE_DPAD_AND_LEFT_STICK
            )
            setOf(LemuroidTouchConfigs.MOTION_SOURCE_RIGHT_DPAD) -> CrossTiltTracker(
                LemuroidTouchConfigs.MOTION_SOURCE_RIGHT_DPAD
            )
            setOf(
                KeyEvent.KEYCODE_BUTTON_L1,
                KeyEvent.KEYCODE_BUTTON_R1
            ) -> TwoButtonsTiltTracker(
                KeyEvent.KEYCODE_BUTTON_L1,
                KeyEvent.KEYCODE_BUTTON_R1
            )
            setOf(
                KeyEvent.KEYCODE_BUTTON_L2,
                KeyEvent.KEYCODE_BUTTON_R2
            ) -> TwoButtonsTiltTracker(
                KeyEvent.KEYCODE_BUTTON_L2,
                KeyEvent.KEYCODE_BUTTON_R2
            )
            else -> null
        }

        eventsTracker?.let { startTrackingId(eventsTracker) }
    }

    override fun onDestroy() {
        stopGameService()
        virtualControllerDisposables.clear()
        super.onDestroy()
    }

    private fun startGameService() {
        serviceController = GameService.startService(applicationContext, game)
    }

    private fun stopGameService() {
        serviceController = GameService.stopService(applicationContext, serviceController)
    }

    override fun onFinishTriggered() {
        super.onFinishTriggered()
        stopGameService()
    }

    private fun handleGamePadButton(it: Event.Button) {
        retroGameView?.sendKeyEvent(it.action, it.id)
    }

    private fun handleGamePadDirection(it: Event.Direction) {
        when (it.id) {
            LemuroidTouchConfigs.MOTION_SOURCE_DPAD -> {
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_DPAD, it.xAxis, it.yAxis)
            }
            LemuroidTouchConfigs.MOTION_SOURCE_LEFT_STICK -> {
                retroGameView?.sendMotionEvent(
                    GLRetroView.MOTION_SOURCE_ANALOG_LEFT,
                    it.xAxis,
                    it.yAxis
                )
            }
            LemuroidTouchConfigs.MOTION_SOURCE_RIGHT_STICK -> {
                retroGameView?.sendMotionEvent(
                    GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,
                    it.xAxis,
                    it.yAxis
                )
            }
            LemuroidTouchConfigs.MOTION_SOURCE_DPAD_AND_LEFT_STICK -> {
                retroGameView?.sendMotionEvent(
                    GLRetroView.MOTION_SOURCE_ANALOG_LEFT,
                    it.xAxis,
                    it.yAxis
                )
                retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_DPAD, it.xAxis, it.yAxis)
            }
            LemuroidTouchConfigs.MOTION_SOURCE_RIGHT_DPAD -> {
                retroGameView?.sendMotionEvent(
                    GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,
                    it.xAxis,
                    it.yAxis
                )
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

        settingsManager.tiltSensitivity
            .autoDispose(scope())
            .subscribeBy { tiltSensor.setSensitivity(it) }

        tiltSensor
            .getTiltEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { sendTiltEvent(it) }

        tiltSensor.isAllowedToRun = true
    }

    private fun sendTiltEvent(sensorValues: FloatArray) {
        currentTiltTracker?.let {
            val xTilt = (sensorValues[0] + 1f) / 2f
            val yTilt = (sensorValues[1] + 1f) / 2f
            it.updateTracking(xTilt, yTilt, sequenceOf(leftPad, rightPad).filterNotNull())
        }
    }

    private fun stopTrackingId(trackedEvent: TiltTracker) {
        currentTiltTracker = null
        tiltSensor.shouldRun = false
        trackedEvent.stopTracking(sequenceOf(leftPad, rightPad).filterNotNull())
    }

    private fun startTrackingId(trackedEvent: TiltTracker) {
        if (currentTiltTracker != trackedEvent) {
            currentTiltTracker?.let { stopTrackingId(it) }
            currentTiltTracker = trackedEvent
            tiltSensor.shouldRun = true
            simulateVirtualGamepadHaptic()
        }
    }

    private fun simulateVirtualGamepadHaptic() {
        leftPad?.performHapticFeedback()
    }

    private fun storeVirtualGamePadSettings(controllerConfig: ControllerConfig, orientation: Int): Completable {
        val virtualGamePadSettingsManager = getVirtualGamePadSettingsManager(controllerConfig, orientation)
        return virtualGamePadSettingsManager.storeSettings(padSettings)
    }

    private fun loadVirtualGamePadSettings(controllerConfig: ControllerConfig, orientation: Int): Completable {
        return getVirtualGamePadSettingsManager(controllerConfig, orientation)
            .retrieveSettings()
            .toMaybe()
            .doOnSuccess { padSettings = it }
            .ignoreElement()
    }

    private fun getVirtualGamePadSettingsManager(
        controllerConfig: ControllerConfig,
        orientation: Int
    ): TouchControllerSettingsManager {
        val settingsOrientation = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            TouchControllerSettingsManager.Orientation.PORTRAIT
        } else {
            TouchControllerSettingsManager.Orientation.LANDSCAPE
        }

        return TouchControllerSettingsManager(
            applicationContext,
            controllerConfig.touchControllerID,
            sharedPreferences,
            settingsOrientation
        )
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
            .filter { it is TouchControllerCustomizer.Event.Save }
            .flatMapCompletable { storeVirtualGamePadSettings(touchControllerConfig!!, orientation) }
    }

    inner class LayoutHandler {

        private fun handleRetroViewLayout(
            constraintSet: ConstraintSet,
            controllerConfig: ControllerConfig,
            orientation: Int,
            virtualPadVisible: Boolean
        ) {
            if (!virtualPadVisible) {
                constraintSet.connect(
                    R.id.gamecontainer,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
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
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                constraintSet.connect(
                    R.id.gamecontainer,
                    ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT
                )
                return
            }

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
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
            padSettings: TouchControllerSettingsManager.Settings,
            controllerConfig: ControllerConfig,
            orientation: Int
        ) {
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

            leftPad.secondaryDialSpacing = 0.1f
            rightPad.secondaryDialSpacing = 0.1f

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

        fun updateLayout(
            config: ControllerConfig,
            padSettings: TouchControllerSettingsManager.Settings,
            orientation: Int,
            virtualPadVisible: Boolean
        ) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainContainerLayout)

            handleVirtualGamePadLayout(constraintSet, padSettings, config, orientation)
            handleRetroViewLayout(constraintSet, config, orientation, virtualPadVisible)

            constraintSet.applyTo(mainContainerLayout)

            mainContainerLayout.requestLayout()
            mainContainerLayout.invalidate()
        }
    }

    companion object {
        const val DEFAULT_MARGINS_DP = 8f
        const val DEFAULT_PRIMARY_DIAL_SIZE = 160f
    }
}
