package come.asg.ominoy.app.shared.game.viewmodel

import android.view.KeyEvent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_ANALOG_LEFT
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_ANALOG_RIGHT
import com.swordfish.libretrodroid.GLRetroView.Companion.MOTION_SOURCE_DPAD
import com.swordfish.touchinput.radial.layouts.shared.ComposeTouchLayouts
import com.swordfish.touchinput.radial.settings.TouchControllerID
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import come.asg.ominoy.app.mobile.feature.settings.SettingsManager
import come.asg.ominoy.app.shared.settings.HapticFeedbackMode
import come.asg.ominoy.common.coroutines.launchOnState
import come.asg.ominoy.common.coroutines.safeCollect
import come.asg.ominoy.lib.controller.ControllerConfig
import gg.padkit.inputevents.InputEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTouchControls(
    private val settingsManager: SettingsManager,
    private val touchControllerSettingsManager: TouchControllerSettingsManager,
    private val retroGameView: GameViewModelRetroGameView,
    private val inputs: GameViewModelInput,
    private val tilt: GameViewModelTilt,
    private val sideEffects: GameViewModelSideEffects,
    private val scope: CoroutineScope,
) : DefaultLifecycleObserver {
    // Null until the real console / orientation are known, so the game never flashes (or stores into)
    // the wrong layout bucket.
    private val touchControlId = MutableStateFlow<TouchControllerID?>(null)
    private val screenOrientation = MutableStateFlow<TouchControllerSettingsManager.Orientation?>(null)
    private val menuPressed = MutableStateFlow(false)
    private val showEditControls = MutableStateFlow(false)
    private val hapticFeedbackMode = MutableStateFlow(HapticFeedbackMode.NONE)
    private var frameSpeedBeforeEditing: Int? = null

    override fun onCreate(owner: LifecycleOwner) {
        owner.launchOnState(Lifecycle.State.CREATED) {
            getTouchControllerConfig().safeCollect {
                touchControlId.value = it.touchControllerID
            }
        }
        owner.launchOnState(Lifecycle.State.CREATED) {
            withContext(Dispatchers.IO) {
                hapticFeedbackMode.value = HapticFeedbackMode.parse(settingsManager.hapticFeedbackMode())
            }
        }
    }

    fun getTouchControlsSettings(
        density: Density,
        insets: WindowInsets,
    ): Flow<TouchControllerSettingsManager.Settings?> =
        combine(
            touchControlId.filterNotNull(),
            screenOrientation.filterNotNull(),
        ) { touchControlId, orientation -> touchControlId to orientation }
            .flatMapLatest { (touchControlId, orientation) ->
                touchControllerSettingsManager.observeSettings(touchControlId, orientation, density, insets)
            }

    fun getTouchControllerSettingsManager(): TouchControllerSettingsManager = touchControllerSettingsManager

    fun getTouchControllerId(): Flow<TouchControllerID?> = touchControlId

    fun getTouchHapticFeedbackMode(): Flow<HapticFeedbackMode> = hapticFeedbackMode

    fun updateTouchControllerSettings(touchControllerSettings: TouchControllerSettingsManager.Settings) {
        val id = touchControlId.value ?: return
        val orientation = screenOrientation.value ?: return
        scope.launch {
            touchControllerSettingsManager.storeSettings(id, orientation, touchControllerSettings)
        }
    }

    /** Atomic in-memory update of one control; call [persistTouchControls] when the gesture ends. */
    fun updateControl(
        key: String,
        transform: (TouchControllerSettingsManager.ControlSettings) -> TouchControllerSettingsManager.ControlSettings,
    ) {
        val id = touchControlId.value ?: return
        val orientation = screenOrientation.value ?: return
        touchControllerSettingsManager.updateControl(id, orientation, key, transform)
    }

    fun persistTouchControls() {
        val id = touchControlId.value ?: return
        val orientation = screenOrientation.value ?: return
        touchControllerSettingsManager.persist(id, orientation)
    }

    fun resetTouchControls() {
        val id = touchControlId.value ?: return
        val orientation = screenOrientation.value ?: return
        scope.launch {
            touchControllerSettingsManager.resetSettings(id, orientation)
        }
    }

    fun updateScreenOrientation(orientation: TouchControllerSettingsManager.Orientation) {
        screenOrientation.value = orientation
    }

    fun isTouchControllerVisible(): Flow<Boolean> =
        inputs
            .getEnabledInputDevices()
            .map { it.isEmpty() }

    fun getTouchControllerConfig(): Flow<ControllerConfig> =
        inputs
            .getControllerConfigState()
            .map { it[0] }
            .filterNotNull()
            .distinctUntilChanged()

    fun handleVirtualInputEvent(events: List<InputEvent>) {
        val menuEvent = events.firstOrNull { it is InputEvent.Button && it.id == KeyEvent.KEYCODE_BUTTON_MODE }
        if (menuEvent != null) {
            onMenuPressed((menuEvent as InputEvent.Button).pressed)
        }

        events.forEach { event ->
            when (event) {
                is InputEvent.Button -> {
                    if (event.id == ComposeTouchLayouts.FAST_FORWARD_BUTTON_ID) {
                        if (event.pressed) {
                            sideEffects.toggleFastForward()
                        }
                    } else {
                        handleVirtualInputButton(event)
                    }
                }

                is InputEvent.DiscreteDirection -> {
                    handleVirtualInputDirection(event.id, event.direction.x, -event.direction.y)
                }

                is InputEvent.ContinuousDirection -> {
                    handleVirtualInputDirection(event.id, event.direction.x, -event.direction.y)
                }
            }
        }
    }

    private fun onMenuPressed(pressed: Boolean) {
        menuPressed.value = pressed

        // A normal tap releases the button before the old 500 ms delay elapsed,
        // which canceled the menu action. Open the menu on button-down instead.
        if (pressed) {
            sideEffects.showMenu(tilt, inputs)
        }
    }

    fun isMenuPressed(): Flow<Boolean> = menuPressed

    fun isEditControlsShown(): Flow<Boolean> = showEditControls

    fun showEditControls(show: Boolean) {
        if (show == showEditControls.value) return
        if (show) {
            // frameSpeed=0 is the LibretroDroid pause state. Keep the old value so opening the
            // editor never changes fast-forward state after the user returns to the game.
            retroGameView.retroGameView?.let { view ->
                frameSpeedBeforeEditing = view.frameSpeed
                view.frameSpeed = 0
            }
        } else {
            persistTouchControls()
            retroGameView.retroGameView?.let { view ->
                view.frameSpeed = frameSpeedBeforeEditing ?: 1
            }
            frameSpeedBeforeEditing = null
        }
        showEditControls.value = show
    }

    private fun handleVirtualInputButton(event: InputEvent.Button) {
        val action = if (event.pressed) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP
        retroGameView.retroGameView?.sendKeyEvent(action, event.id)
    }

    private fun handleVirtualInputDirection(
        id: Int,
        xAxis: Float,
        yAxis: Float,
    ) {
        when (id) {
            ComposeTouchLayouts.MOTION_SOURCE_DPAD -> {
                retroGameView.retroGameView?.sendMotionEvent(GLRetroView.MOTION_SOURCE_DPAD, xAxis, yAxis)
            }

            ComposeTouchLayouts.MOTION_SOURCE_LEFT_STICK -> {
                retroGameView.retroGameView?.sendMotionEvent(
                    MOTION_SOURCE_ANALOG_LEFT,
                    xAxis,
                    yAxis,
                )
            }

            ComposeTouchLayouts.MOTION_SOURCE_RIGHT_STICK -> {
                retroGameView.retroGameView?.sendMotionEvent(
                    MOTION_SOURCE_ANALOG_RIGHT,
                    xAxis,
                    yAxis,
                )
            }

            ComposeTouchLayouts.MOTION_SOURCE_DPAD_AND_LEFT_STICK -> {
                retroGameView.retroGameView?.sendMotionEvent(
                    MOTION_SOURCE_ANALOG_LEFT,
                    xAxis,
                    yAxis,
                )
                retroGameView.retroGameView?.sendMotionEvent(MOTION_SOURCE_DPAD, xAxis, yAxis)
            }

            ComposeTouchLayouts.MOTION_SOURCE_RIGHT_DPAD -> {
                retroGameView.retroGameView?.sendMotionEvent(
                    MOTION_SOURCE_ANALOG_RIGHT,
                    xAxis,
                    yAxis,
                )
            }
        }
    }

    companion object {
        const val MENU_LOADING_ANIMATION_MILLIS = 500
    }
}
