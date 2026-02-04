package com.swordfish.lemuroid.lib.library

import com.swordfish.lemuroid.lib.R
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.touchinput.radial.sensors.TILT_CONFIGURATION_ANALOG_LEFT
import com.swordfish.touchinput.radial.sensors.TILT_CONFIGURATION_ANALOG_RIGHT
import com.swordfish.touchinput.radial.sensors.TILT_CONFIGURATION_CROSS
import com.swordfish.touchinput.radial.sensors.TILT_CONFIGURATION_DISABLED
import com.swordfish.touchinput.radial.sensors.TILT_CONFIGURATION_L1_R1
import com.swordfish.touchinput.radial.sensors.TILT_CONFIGURATION_L2_R2
import com.swordfish.touchinput.radial.sensors.TILT_CONFIGURATION_L_R
import com.swordfish.touchinput.radial.settings.TouchControllerID

// TODO PADS... Make sure the ids are correct.
object ControllerConfigs {
    val ATARI_2600 =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.ATARI2600,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val NES =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.NES,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val SNES =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.SNES,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                    TILT_CONFIGURATION_L_R,
                ),
        )

    val SMS =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.SMS,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val GENESIS_6 =
        ControllerConfig(
            "default_6",
            R.string.controller_genesis_6,
            TouchControllerID.GENESIS_6,
            mergeDPADAndLeftStickEvents = true,
            libretroDescriptor = "MD Joypad 6 Button",
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val GENESIS_3 =
        ControllerConfig(
            "default_3",
            R.string.controller_genesis_3,
            TouchControllerID.GENESIS_3,
            mergeDPADAndLeftStickEvents = true,
            libretroDescriptor = "MD Joypad 3 Button",
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val GG =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.GG,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val GB =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.GB,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val GBA =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.GBA,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                    TILT_CONFIGURATION_L_R,
                ),
        )

    val N64 =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.N64,
            allowTouchRotation = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                    TILT_CONFIGURATION_ANALOG_LEFT,
                    TILT_CONFIGURATION_L_R,
                ),
        )

    val PSX_STANDARD =
        ControllerConfig(
            "standard",
            R.string.controller_standard,
            TouchControllerID.PSX,
            mergeDPADAndLeftStickEvents = true,
            libretroDescriptor = "standard",
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                    TILT_CONFIGURATION_L1_R1,
                    TILT_CONFIGURATION_L2_R2,
                ),
        )

    val PSX_DUALSHOCK =
        ControllerConfig(
            "dualshock",
            R.string.controller_dualshock,
            TouchControllerID.PSX_DUALSHOCK,
            allowTouchRotation = true,
            libretroDescriptor = "dualshock",
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                    TILT_CONFIGURATION_ANALOG_LEFT,
                    TILT_CONFIGURATION_ANALOG_RIGHT,
                    TILT_CONFIGURATION_L1_R1,
                    TILT_CONFIGURATION_L2_R2,
                ),
        )

    val PSP =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.PSP,
            allowTouchRotation = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                    TILT_CONFIGURATION_ANALOG_LEFT,
                    TILT_CONFIGURATION_L_R,
                ),
        )

    val FB_NEO_4 =
        ControllerConfig(
            "default_4",
            R.string.controller_arcade_4,
            TouchControllerID.ARCADE_4,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val FB_NEO_6 =
        ControllerConfig(
            "default_6",
            R.string.controller_arcade_6,
            TouchControllerID.ARCADE_6,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val MAME_2003_4 =
        ControllerConfig(
            "default_4",
            R.string.controller_arcade_4,
            TouchControllerID.ARCADE_4,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val MAME_2003_6 =
        ControllerConfig(
            "default_6",
            R.string.controller_arcade_6,
            TouchControllerID.ARCADE_6,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val DESMUME =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.DESMUME,
            allowTouchOverlay = false,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                    TILT_CONFIGURATION_L_R,
                ),
        )

    val MELONDS =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.MELONDS,
            mergeDPADAndLeftStickEvents = true,
            allowTouchOverlay = false,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                    TILT_CONFIGURATION_L_R,
                ),
        )

    val LYNX =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.LYNX,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val ATARI7800 =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.ATARI7800,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val PCE =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.PCE,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                    TILT_CONFIGURATION_L_R,
                ),
        )

    val NGP =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.NGP,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val DOS_AUTO =
        ControllerConfig(
            "auto",
            R.string.controller_dos_auto,
            TouchControllerID.DOS,
            allowTouchRotation = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                    TILT_CONFIGURATION_ANALOG_LEFT,
                    TILT_CONFIGURATION_ANALOG_RIGHT,
                    TILT_CONFIGURATION_L1_R1,
                    TILT_CONFIGURATION_L2_R2,
                ),
        )

    val WS_LANDSCAPE =
        ControllerConfig(
            "landscape",
            R.string.controller_landscape,
            TouchControllerID.WS_LANDSCAPE,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val WS_PORTRAIT =
        ControllerConfig(
            "portrait",
            R.string.controller_portrait,
            TouchControllerID.WS_PORTRAIT,
            mergeDPADAndLeftStickEvents = true,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                ),
        )

    val NINTENDO_3DS =
        ControllerConfig(
            "default",
            R.string.controller_default,
            TouchControllerID.NINTENDO_3DS,
            allowTouchOverlay = false,
            tiltConfigurations =
                listOf(
                    TILT_CONFIGURATION_DISABLED,
                    TILT_CONFIGURATION_CROSS,
                    TILT_CONFIGURATION_ANALOG_LEFT,
                    TILT_CONFIGURATION_L_R,
                ),
        )
}
