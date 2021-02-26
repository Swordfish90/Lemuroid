package com.swordfish.lemuroid.lib.library

import com.swordfish.lemuroid.lib.R
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.controller.TouchControllerID

object ControllerConfigs {

    val ATARI_2600 = ControllerConfig(
        "default",
        R.string.controller_default,
        TouchControllerID.ATARI2600,
        mergeDPADAndLeftStickEvents = true
    )

    val NES = ControllerConfig(
        "default",
        R.string.controller_default,
        TouchControllerID.NES,
        mergeDPADAndLeftStickEvents = true
    )

    val SNES = ControllerConfig(
        "default",
        R.string.controller_default,
        TouchControllerID.SNES,
        mergeDPADAndLeftStickEvents = true
    )

    val SMS = ControllerConfig(
        "default",
        R.string.controller_default,
        TouchControllerID.SMS,
        mergeDPADAndLeftStickEvents = true
    )

    val GENESIS_6 = ControllerConfig(
        "default_6",
        R.string.controller_genesis_6,
        TouchControllerID.GENESIS_6,
        mergeDPADAndLeftStickEvents = true,
        libretroDescriptor = "MD Joypad 6 Button"
    )

    val GENESIS_3 = ControllerConfig(
        "default_3",
        R.string.controller_genesis_3,
        TouchControllerID.GENESIS_3,
        mergeDPADAndLeftStickEvents = true,
        libretroDescriptor = "MD Joypad 3 Button"
    )

    val GG = ControllerConfig(
        "default",
        R.string.controller_default,
        TouchControllerID.GG,
        mergeDPADAndLeftStickEvents = true
    )

    val GB = ControllerConfig(
        "default",
        R.string.controller_default,
        TouchControllerID.GB,
        mergeDPADAndLeftStickEvents = true
    )

    val GBA = ControllerConfig(
        "default",
        R.string.controller_default,
        TouchControllerID.GBA,
        mergeDPADAndLeftStickEvents = true
    )

    val N64 = ControllerConfig(
        "default",
        R.string.controller_default,
        TouchControllerID.N64,
        allowTouchRotation = true
    )

    val PSX_STANDARD = ControllerConfig(
        "standard",
        R.string.controller_standard,
        TouchControllerID.PSX,
        mergeDPADAndLeftStickEvents = true,
        libretroDescriptor = "standard"
    )

    val PSX_DUALSHOCK = ControllerConfig(
        "dualshock",
        R.string.controller_dualshock,
        TouchControllerID.PSX_DUALSHOCK,
        allowTouchRotation = true,
        libretroDescriptor = "dualshock"
    )

    val PSP = ControllerConfig(
        "default",
        R.string.controller_default,
        TouchControllerID.PSP,
        allowTouchRotation = true,
    )

    val FB_NEO_4 = ControllerConfig(
        "default_4",
        R.string.controller_arcade_4,
        TouchControllerID.ARCADE_4,
        mergeDPADAndLeftStickEvents = true
    )

    val FB_NEO_6 = ControllerConfig(
        "default_6",
        R.string.controller_arcade_6,
        TouchControllerID.ARCADE_6,
        mergeDPADAndLeftStickEvents = true
    )

    val MAME_2003_4 = ControllerConfig(
        "default_4",
        R.string.controller_arcade_4,
        TouchControllerID.ARCADE_4,
        mergeDPADAndLeftStickEvents = true
    )

    val MAME_2003_6 = ControllerConfig(
        "default_6",
        R.string.controller_arcade_6,
        TouchControllerID.ARCADE_6,
        mergeDPADAndLeftStickEvents = true
    )

    val DESMUME = ControllerConfig(
        "default",
        R.string.controller_default,
        TouchControllerID.DESMUME,
        allowTouchOverlay = false
    )

    val MELONDS = ControllerConfig(
        "default",
        R.string.controller_default,
        TouchControllerID.MELONDS,
        mergeDPADAndLeftStickEvents = true,
        allowTouchOverlay = false
    )
}
