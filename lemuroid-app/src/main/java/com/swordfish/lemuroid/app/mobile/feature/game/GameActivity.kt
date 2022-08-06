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
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.lemuroid.app.mobile.feature.tilt.CrossTiltTracker
import com.swordfish.lemuroid.app.mobile.feature.tilt.StickTiltTracker
import com.swordfish.lemuroid.app.mobile.feature.tilt.TiltTracker
import com.swordfish.lemuroid.app.mobile.feature.tilt.TwoButtonsTiltTracker
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.common.coroutines.batchWithTime
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.common.coroutines.safeCollect
import com.swordfish.lemuroid.common.graphics.GraphicsUtils
import com.swordfish.lemuroid.common.kotlin.NTuple5
import com.swordfish.lemuroid.common.math.linearInterpolation
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.controller.TouchControllerCustomizer
import com.swordfish.lemuroid.lib.controller.TouchControllerSettingsManager
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.radialgamepad.library.RadialGamePad
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.HapticConfig
import com.swordfish.touchinput.radial.LemuroidTouchConfigs
import com.swordfish.touchinput.radial.LemuroidTouchOverlayThemes
import com.swordfish.touchinput.radial.sensors.TiltSensor
import dagger.Lazy
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.withContext

class GameActivity : BaseGameActivity() {
    @Inject
    lateinit var sharedPreferences: Lazy<SharedPreferences>

    private lateinit var horizontalDivider: View
    private lateinit var leftVerticalDivider: View
    private lateinit var rightVerticalDivider: View

    private var serviceController: GameService.GameServiceController? = null

    private lateinit var tiltSensor: TiltSensor
    private var currentTiltTracker: TiltTracker? = null

    private var leftPad: RadialGamePad? = null
    private var rightPad: RadialGamePad? = null

    private val virtualControllerJobs = mutableSetOf<Job>()

    private val touchControllerConfigState = MutableStateFlow<ControllerConfig?>(null)
    private val padSettingsState = MutableStateFlow<TouchControllerSettingsManager.Settings?>(null)
    private val insetsState = MutableStateFlow<Rect?>(null)
    private val orientationState = MutableStateFlow(Configuration.ORIENTATION_PORTRAIT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orientationState.value = getCurrentOrientation()

        tiltSensor = TiltSensor(applicationContext)

        horizontalDivider = findViewById(R.id.horizontaldividier)
        leftVerticalDivider = findViewById(R.id.leftverticaldivider)
        rightVerticalDivider = findViewById(R.id.rightverticaldivider)

        initializeInsetsState()

        startGameService()

        initializeFlows()
    }

    private fun initializeFlows() {
        launchOnState(Lifecycle.State.CREATED) {
            initializeVirtualGamePadFlow()
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeLayoutChangeFlow()
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeTiltSensitivityFlow()
        }

        launchOnState(Lifecycle.State.CREATED) {
            initializeVirtualControlsVisibilityFlow()
        }

        launchOnState(Lifecycle.State.RESUMED) {
            initializeTiltEventsFlow()
        }
    }

    private suspend fun initializeVirtualControlsVisibilityFlow() {
        isVirtualGamePadVisible()
            .safeCollect {
                leftGamePadContainer.isVisible = it
                rightGamePadContainer.isVisible = it
            }
    }

    private suspend fun initializeLayoutChangeFlow() {
        val combinedFlow = combine(
            touchControllerConfigState.filterNotNull(),
            orientationState,
            isVirtualGamePadVisible(),
            padSettingsState.filterNotNull(),
            insetsState.filterNotNull(),
            ::NTuple5
        )
        combinedFlow.safeCollect { (config, orientation, virtualGamePadVisible, padSettings, insets) ->
            LayoutHandler().updateLayout(config, padSettings, orientation, virtualGamePadVisible, insets)
        }
    }

    private suspend fun initializeTiltEventsFlow() {
        tiltSensor
            .getTiltEvents()
            .safeCollect { sendTiltEvent(it) }
    }

    private suspend fun initializeTiltSensitivityFlow() {
        val sensitivity = settingsManager.tiltSensitivity()
        tiltSensor.setSensitivity(sensitivity)
    }

    private fun initializeInsetsState() {
        mainContainerLayout.setOnApplyWindowInsetsListener { _, windowInsets ->
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val insets = windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.displayCutout()
                )
                Rect(insets.left, insets.top, insets.right, insets.bottom)
            } else {
                Rect(0, 0, 0, 0)
            }
            insetsState.value = result
            windowInsets
        }
    }

    private suspend fun initializeVirtualGamePadFlow() {
        val firstGamePad = getControllerType()
            .map { it[0] }
            .filterNotNull()
            .distinctUntilChanged()

        combine(firstGamePad, orientationState, ::Pair)
            .safeCollect { (pad, orientation) ->
                setupController(pad, orientation)
            }
    }

    private suspend fun setupController(controllerConfig: ControllerConfig, orientation: Int) {
        val hapticFeedbackMode = settingsManager.hapticFeedbackMode()
        withContext(Dispatchers.Main) {
            setupTouchViews(controllerConfig, hapticFeedbackMode, orientation)
        }
        loadVirtualGamePadSettings(controllerConfig, orientation)
    }

    private fun isVirtualGamePadVisible(): Flow<Boolean> {
        return inputDeviceManager
            .getEnabledInputsObservable()
            .map { it.isEmpty() }
    }

    private fun getCurrentOrientation() = resources.configuration.orientation

    override fun getDialogClass() = GameMenuActivity::class.java

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        orientationState.value = newConfig.orientation
    }

    private fun setupTouchViews(
        controllerConfig: ControllerConfig,
        hapticFeedbackType: String,
        orientation: Int
    ) {
        virtualControllerJobs
            .forEach { it.cancel() }
        virtualControllerJobs.clear()

        leftGamePadContainer.removeAllViews()
        rightGamePadContainer.removeAllViews()

        val touchControllerConfig = controllerConfig.getTouchControllerConfig()

        val hapticConfig = when (hapticFeedbackType) {
            "none" -> HapticConfig.OFF
            "press" -> HapticConfig.PRESS
            "press_release" -> HapticConfig.PRESS_AND_RELEASE
            else -> HapticConfig.OFF
        }

        val theme = LemuroidTouchOverlayThemes.getGamePadTheme(leftGamePadContainer)

        updateDividers(orientation, theme, controllerConfig)

        val leftConfig = LemuroidTouchConfigs.getRadialGamePadConfig(
            touchControllerConfig.leftConfig,
            hapticConfig,
            theme
        )
        val leftPad = RadialGamePad(leftConfig, DEFAULT_MARGINS_DP, this)
        leftGamePadContainer.addView(leftPad)

        val rightConfig = LemuroidTouchConfigs.getRadialGamePadConfig(
            touchControllerConfig.rightConfig,
            hapticConfig,
            theme
        )
        val rightPad = RadialGamePad(rightConfig, DEFAULT_MARGINS_DP, this)
        rightGamePadContainer.addView(rightPad)

        val virtualPadEvents = merge(leftPad.events().asFlow(), rightPad.events().asFlow())
            .shareIn(lifecycleScope, SharingStarted.Lazily)

        setupDefaultActions(virtualPadEvents)
        setupTiltActions(virtualPadEvents)
        setupVirtualMenuActions(virtualPadEvents)

        this.leftPad = leftPad
        this.rightPad = rightPad

        touchControllerConfigState.value = controllerConfig
    }

    private fun updateDividers(
        orientation: Int,
        theme: RadialGamePadTheme,
        controllerConfig: ControllerConfig
    ) {
        val displayHorizontalDivider = orientation == Configuration.ORIENTATION_PORTRAIT

        val displayVerticalDivider = orientation != Configuration.ORIENTATION_PORTRAIT &&
            !controllerConfig.allowTouchOverlay

        updateDivider(horizontalDivider, displayHorizontalDivider, theme)
        updateDivider(leftVerticalDivider, displayVerticalDivider, theme)
        updateDivider(rightVerticalDivider, displayVerticalDivider, theme)
    }

    private fun updateDivider(divider: View, visible: Boolean, theme: RadialGamePadTheme) {
        divider.isVisible = visible
        divider.setBackgroundColor(theme.backgroundStrokeColor)
    }

    private fun setupDefaultActions(virtualPadEvents: Flow<Event>) {
        val job = lifecycleScope.launch {
            virtualPadEvents
                .safeCollect {
                    when (it) {
                        is Event.Button -> {
                            handleGamePadButton(it)
                        }
                        is Event.Direction -> {
                            handleGamePadDirection(it)
                        }
                    }
                }
        }
        virtualControllerJobs.add(job)
    }

    private fun setupTiltActions(virtualPadEvents: Flow<Event>) {
        val job1 = lifecycleScope.launch {
            virtualPadEvents
                .filterIsInstance<Event.Gesture>()
                .filter { it.type == GestureType.TRIPLE_TAP }
                .batchWithTime(500)
                .filter { it.isNotEmpty() }
                .safeCollect { events ->
                    handleTripleTaps(events)
                }
        }

        val job2 = lifecycleScope.launch {
            virtualPadEvents
                .filterIsInstance<Event.Gesture>()
                .filter { it.type == GestureType.FIRST_TOUCH }
                .safeCollect { event ->
                    currentTiltTracker?.let { tracker ->
                        if (event.id in tracker.trackedIds()) {
                            stopTrackingId(tracker)
                        }
                    }
                }
        }

        virtualControllerJobs.add(job1)
        virtualControllerJobs.add(job2)
    }

    private fun setupVirtualMenuActions(virtualPadEvents: Flow<Event>) {
        VirtualLongPressHandler.initializeTheme(this)

        val allMenuButtonEvents = virtualPadEvents
            .filterIsInstance<Event.Button>()
            .filter { it.id == KeyEvent.KEYCODE_BUTTON_MODE }
            .shareIn(lifecycleScope, SharingStarted.Lazily)

        val cancelMenuButtonEvents = allMenuButtonEvents
            .filter { it.action == KeyEvent.ACTION_UP }
            .map { Unit }

        val job = lifecycleScope.launch {
            allMenuButtonEvents
                .filter { it.action == KeyEvent.ACTION_DOWN }
                .map {
                    VirtualLongPressHandler.displayLoading(
                        this@GameActivity,
                        R.drawable.ic_menu,
                        cancelMenuButtonEvents
                    )
                }
                .filter { it }
                .safeCollect {
                    displayOptionsDialog()
                    simulateVirtualGamepadHaptic()
                }
        }

        virtualControllerJobs.add(job)
    }

    private fun handleTripleTaps(events: List<Event.Gesture>) {
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
        virtualControllerJobs.clear()
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
                lifecycleScope.launch {
                    displayCustomizationOptions()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        tiltSensor.isAllowedToRun = false
    }

    override fun onResume() {
        super.onResume()
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

    private suspend fun storeVirtualGamePadSettings(
        controllerConfig: ControllerConfig,
        orientation: Int
    ) {
        val virtualGamePadSettingsManager = getVirtualGamePadSettingsManager(controllerConfig, orientation)
        return virtualGamePadSettingsManager.storeSettings(padSettingsState.value!!)
    }

    private suspend fun loadVirtualGamePadSettings(
        controllerConfig: ControllerConfig,
        orientation: Int
    ) {
        val virtualGamePadSettingsManager =
            getVirtualGamePadSettingsManager(controllerConfig, orientation)
        padSettingsState.value = virtualGamePadSettingsManager.retrieveSettings()
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

    private suspend fun displayCustomizationOptions() {
        findViewById<View>(R.id.editcontrolsdarkening).isVisible = true

        val customizer = TouchControllerCustomizer()

        val insets = insetsState
            .filterNotNull()
            .first()

        val touchControllerConfig = touchControllerConfigState
            .filterNotNull()
            .first()

        val padSettings = padSettingsState.filterNotNull()
            .first()

        val initialSettings = TouchControllerCustomizer.Settings(
            padSettings.scale,
            padSettings.rotation,
            padSettings.marginX,
            padSettings.marginY
        )

        customizer.displayCustomizationPopup(
            this@GameActivity,
            layoutInflater,
            mainContainerLayout,
            insets,
            initialSettings
        )
            .takeWhile { it !is TouchControllerCustomizer.Event.Close }
            .onEach {
                val current = padSettingsState.filterNotNull().first()
                when (it) {
                    is TouchControllerCustomizer.Event.Scale -> {
                        padSettingsState.value = current.copy(scale = it.value)
                    }
                    is TouchControllerCustomizer.Event.Rotation -> {
                        padSettingsState.value = current.copy(rotation = it.value)
                    }
                    is TouchControllerCustomizer.Event.Margins -> {
                        padSettingsState.value = current.copy(marginX = it.x, marginY = it.y)
                    }
                    else -> Unit
                }
            }
            .safeCollect { }

        storeVirtualGamePadSettings(touchControllerConfig, orientationState.value)
        findViewById<View>(R.id.editcontrolsdarkening).isVisible = false
    }

    inner class LayoutHandler {

        private fun handleRetroViewLayout(
            constraintSet: ConstraintSet,
            controllerConfig: ControllerConfig,
            orientation: Int,
            virtualPadVisible: Boolean,
            insets: Rect
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
                    R.id.horizontaldividier,
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
                        R.id.leftverticaldivider,
                        ConstraintSet.RIGHT
                    )

                    constraintSet.connect(
                        R.id.gamecontainer,
                        ConstraintSet.RIGHT,
                        R.id.rightverticaldivider,
                        ConstraintSet.LEFT
                    )
                }
            }

            constraintSet.constrainedWidth(R.id.gamecontainer, true)
            constraintSet.constrainedHeight(R.id.gamecontainer, true)

            constraintSet.setMargin(R.id.gamecontainer, ConstraintSet.TOP, insets.top)
        }

        private fun handleVirtualGamePadLayout(
            constraintSet: ConstraintSet,
            padSettings: TouchControllerSettingsManager.Settings,
            controllerConfig: ControllerConfig,
            orientation: Int,
            insets: Rect
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

            val baseVerticalMargin = GraphicsUtils.convertDpToPixel(
                touchControllerConfig.verticalMarginDP,
                applicationContext
            )

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                setupMarginsForPortrait(
                    leftPad,
                    rightPad,
                    maxMargins,
                    padSettings,
                    baseVerticalMargin.roundToInt() + insets.bottom
                )
            } else {
                setupMarginsForLandscape(
                    leftPad,
                    rightPad,
                    maxMargins,
                    padSettings,
                    baseVerticalMargin.roundToInt() + insets.bottom,
                    maxOf(insets.left, insets.right)
                )
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

        private fun setupMarginsForLandscape(
            leftPad: RadialGamePad,
            rightPad: RadialGamePad,
            maxMargins: Float,
            padSettings: TouchControllerSettingsManager.Settings,
            verticalSpacing: Int,
            horizontalSpacing: Int
        ) {
            leftPad.spacingBottom = verticalSpacing
            leftPad.spacingLeft = linearInterpolation(
                padSettings.marginX,
                0f,
                maxMargins
            ).roundToInt() + horizontalSpacing

            rightPad.spacingBottom = verticalSpacing
            rightPad.spacingRight = linearInterpolation(
                padSettings.marginX,
                0f,
                maxMargins
            ).roundToInt() + horizontalSpacing

            leftPad.offsetX = 0f
            rightPad.offsetX = 0f

            leftPad.offsetY = -linearInterpolation(padSettings.marginY, 0f, maxMargins)
            rightPad.offsetY = -linearInterpolation(padSettings.marginY, 0f, maxMargins)
        }

        private fun setupMarginsForPortrait(
            leftPad: RadialGamePad,
            rightPad: RadialGamePad,
            maxMargins: Float,
            padSettings: TouchControllerSettingsManager.Settings,
            verticalSpacing: Int
        ) {
            leftPad.spacingBottom = linearInterpolation(
                padSettings.marginY,
                0f,
                maxMargins
            ).roundToInt() + verticalSpacing
            leftPad.spacingLeft = 0
            rightPad.spacingBottom = linearInterpolation(
                padSettings.marginY,
                0f,
                maxMargins
            ).roundToInt() + verticalSpacing
            rightPad.spacingRight = 0

            leftPad.offsetX = linearInterpolation(padSettings.marginX, 0f, maxMargins)
            rightPad.offsetX = -linearInterpolation(padSettings.marginX, 0f, maxMargins)

            leftPad.offsetY = 0f
            rightPad.offsetY = 0f
        }

        fun updateLayout(
            config: ControllerConfig,
            padSettings: TouchControllerSettingsManager.Settings,
            orientation: Int,
            virtualPadVisible: Boolean,
            insets: Rect
        ) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainContainerLayout)

            handleVirtualGamePadLayout(constraintSet, padSettings, config, orientation, insets)
            handleRetroViewLayout(constraintSet, config, orientation, virtualPadVisible, insets)

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
