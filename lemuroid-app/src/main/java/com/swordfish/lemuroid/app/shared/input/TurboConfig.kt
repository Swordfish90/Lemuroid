package com.swordfish.lemuroid.app.shared.input

import android.view.KeyEvent

/**
 * Per-device turbo (rapid fire) configuration.
 *
 * A turbo binding maps a *target* RetroPad key (the button that will be rapidly pressed, e.g. A)
 * to a *trigger* physical input key (the button the user holds to fire, e.g. the gamepad X button).
 * While the trigger key is held, the runtime repeatedly injects down/up events for the target key.
 *
 * Persisted per device as three independent preferences (enabled / frequency / bindings) by
 * [InputDeviceManager]; this type is the in-memory aggregate and is never serialized as a whole.
 */
data class TurboConfig(
    val enabled: Boolean = false,
    val frequencyHz: Int = DEFAULT_FREQUENCY_HZ,
    // target RetroPad keyCode -> trigger physical input keyCode
    val bindings: Map<Int, Int> = DEFAULT_BINDINGS,
) {
    /** Returns the target RetroPad keyCode turbo-fired by [triggerKeyCode], or null if it is not a turbo trigger. */
    fun targetForTrigger(triggerKeyCode: Int): Int? {
        if (!enabled) return null
        return bindings.entries.firstOrNull { it.value == triggerKeyCode }?.key
    }

    /** Returns the physical trigger keyCode currently bound to the given [targetKeyCode]. */
    fun triggerForTarget(targetKeyCode: Int): Int? = bindings[targetKeyCode]

    companion object {
        const val DEFAULT_FREQUENCY_HZ = 20
        const val MIN_FREQUENCY_HZ = 5
        const val MAX_FREQUENCY_HZ = 30

        // On NES/FC only A and B are used, so the (otherwise idle) X and Y gamepad buttons
        // are a natural fit for turbo A / turbo B.
        val DEFAULT_BINDINGS: Map<Int, Int> =
            mapOf(
                KeyEvent.KEYCODE_BUTTON_A to KeyEvent.KEYCODE_BUTTON_X,
                KeyEvent.KEYCODE_BUTTON_B to KeyEvent.KEYCODE_BUTTON_Y,
            )

        // The turbo targets a user can configure. NES only needs A and B.
        val CONFIGURABLE_TARGETS: List<Int> =
            listOf(
                KeyEvent.KEYCODE_BUTTON_A,
                KeyEvent.KEYCODE_BUTTON_B,
            )
    }
}
