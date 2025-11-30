package com.swordfish.lemuroid.app.mobile.feature.gamemenu.tilt

import android.view.KeyEvent
import com.swordfish.lemuroid.R
import com.swordfish.touchinput.radial.sensors.TiltConfiguration

enum class TiltConfigurationMenuEntry(val configuration: TiltConfiguration, val descriptionId: Int) {
    TILT_CONFIGURATION_DISABLED(
        TiltConfiguration.Disabled,
        R.string.tilt_configuration_disabled,
    ),
    TILT_CONFIGURATION_CROSS(
        TiltConfiguration.Cross(0),
        R.string.tilt_configuration_cross,
    ),
    TILT_CONFIGURATION_ANALOG_LEFT(
        TiltConfiguration.Analog(1),
        R.string.tilt_configuration_analog_left,
    ),
    TILT_CONFIGURATION_ANALOG_RIGHT(
        TiltConfiguration.Analog(2),
        R.string.tilt_configuration_analog_right,
    ),
    TILT_CONFIGURATION_L_R(
        TiltConfiguration.ButtonPair(KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_R1),
        R.string.tilt_configuration_buttons_l_r,
    ),
    TILT_CONFIGURATION_L1_R1(
        TiltConfiguration.ButtonPair(KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_R1),
        R.string.tilt_configuration_buttons_l1_r1,
    ),
    TILT_CONFIGURATION_L2_R2(
        TiltConfiguration.ButtonPair(KeyEvent.KEYCODE_BUTTON_L2, KeyEvent.KEYCODE_BUTTON_R2),
        R.string.tilt_configuration_buttons_l2_r2,
    ),
    ;

    companion object {
        fun fromTiltConfiguration(tiltConfiguration: TiltConfiguration): TiltConfigurationMenuEntry {
            return TiltConfigurationMenuEntry.entries
                .firstOrNull { it.configuration == tiltConfiguration }
                ?: TILT_CONFIGURATION_DISABLED
        }
    }
}
