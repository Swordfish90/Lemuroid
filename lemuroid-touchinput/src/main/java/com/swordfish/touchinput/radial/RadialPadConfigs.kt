package com.swordfish.touchinput.radial

import android.view.KeyEvent
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.PrimaryDialConfig
import com.swordfish.radialgamepad.library.config.SecondaryDialConfig
import com.swordfish.radialgamepad.library.config.CrossContentDescription
import com.swordfish.touchinput.controller.R

object RadialPadConfigs {

    const val MOTION_SOURCE_DPAD = 0
    const val MOTION_SOURCE_LEFT_STICK = 1
    const val MOTION_SOURCE_RIGHT_STICK = 2
    const val MOTION_SOURCE_DPAD_AND_LEFT_STICK = 3

    private val BUTTON_CONFIG_START = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_START,
            iconId = R.drawable.button_start,
            contentDescription = "Start"
    )

    private val BUTTON_CONFIG_SELECT = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_SELECT,
            iconId = R.drawable.button_select,
            contentDescription = "Select"
    )

    private val BUTTON_CONFIG_MENU = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_MODE,
            iconId = R.drawable.button_menu,
            contentDescription = "Menu"
    )

    private val BUTTON_CONFIG_CROSS = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_B,
            iconId = R.drawable.psx_cross,
            contentDescription = "Cross"
    )

    private val BUTTON_CONFIG_SQUARE = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_Y,
            iconId = R.drawable.psx_square,
            contentDescription = "Square"
    )

    private val BUTTON_CONFIG_TRIANGLE = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_X,
            iconId = R.drawable.psx_triangle,
            contentDescription = "Triangle"
    )

    private val BUTTON_CONFIG_CIRCLE = ButtonConfig(
            id = KeyEvent.KEYCODE_BUTTON_A,
            iconId = R.drawable.psx_circle,
            contentDescription = "Circle"
    )

    val GB_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                            SecondaryDialConfig.Empty(8, 1, 1f)
                    )
            )

    val GB_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            dials = listOf(
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_A,
                                            label = "A"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_B,
                                            label = "B"
                                    )
                            ),
                            rotationInDegrees = 30f
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
                    )
            )

    val NES_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                            SecondaryDialConfig.Empty(10, 1, 1f)
                    )
            )

    val NES_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            dials = listOf(
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_A,
                                            label = "A"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_B,
                                            label = "B"
                                    )
                            )
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
                    )
            )

    val NDS_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_THUMBL,
                                            iconId = R.drawable.button_blow,
                                            contentDescription = "Microphone"
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(3, 1, BUTTON_CONFIG_SELECT),
                            SecondaryDialConfig.SingleButton(4, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_L1,
                                            label = "L"
                                    )
                            )
                    )
            )

    val NDS_RIGHT = RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                    dials = listOf(
                            ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_A,
                                    label = "A"
                            ),
                            ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_X,
                                    label = "X"
                            ),
                            ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_Y,
                                    label = "Y"
                            ),
                            ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_B,
                                    label = "B"
                            )
                    )
            ),
            secondaryDials = listOf(
                    SecondaryDialConfig.SingleButton(2, 1,
                            ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_R1,
                                    label = "R"
                            )
                    ),
                    SecondaryDialConfig.SingleButton(3, 1, BUTTON_CONFIG_START),
                    SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_MENU)
            )
    )

    val PSX_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(
                                    2, 1, BUTTON_CONFIG_SELECT
                            ),
                            SecondaryDialConfig.SingleButton(
                                    3, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_L1,
                                    label = "L1"
                            )
                            ),
                            SecondaryDialConfig.SingleButton(
                                    4, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_L2,
                                    label = "L2"
                            )
                            ),
                            SecondaryDialConfig.Stick(
                                    9,
                                    2.2f,
                                    MOTION_SOURCE_LEFT_STICK,
                                    KeyEvent.KEYCODE_BUTTON_THUMBL,
                                    contentDescription = "Left Stick"
                            ),
                            SecondaryDialConfig.Empty(8, 1, 1f)
                    )
            )

    val PSX_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            listOf(
                                    BUTTON_CONFIG_CIRCLE,
                                    BUTTON_CONFIG_TRIANGLE,
                                    BUTTON_CONFIG_SQUARE,
                                    BUTTON_CONFIG_CROSS
                            )
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(
                                    2, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_R2,
                                    label = "R2"
                            )
                            ),
                            SecondaryDialConfig.SingleButton(
                                    3, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_R1,
                                    label = "R1"
                            )
                            ),
                            SecondaryDialConfig.SingleButton(
                                    4, 1, BUTTON_CONFIG_START
                            ),
                            SecondaryDialConfig.Stick(
                                    8,
                                    2.2f,
                                    MOTION_SOURCE_RIGHT_STICK,
                                    KeyEvent.KEYCODE_BUTTON_THUMBR,
                                    contentDescription = "Right Stick"
                            ),
                            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
                    )
            )

    val N64_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.Empty(2, 1, 1f),
                            SecondaryDialConfig.SingleButton(
                                    2, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_L2,
                                    label = "Z"
                            )
                            ),
                            SecondaryDialConfig.SingleButton(
                                    3, 2, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_L1,
                                    label = "L"
                            )
                            ),
                            SecondaryDialConfig.Empty(8, 1, 1f),
                            SecondaryDialConfig.Stick(9, 2.2f, MOTION_SOURCE_LEFT_STICK)
                    )
            )

    val N64_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            listOf(
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_L2,
                                            label = "Z"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_Y,
                                            label = "B"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_B,
                                            label = "A"
                                    )
                            ),
                            rotationInDegrees = 60f
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(
                                    2, 2, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_R1,
                                    label = "R"
                            )
                            ),
                            SecondaryDialConfig.SingleButton(
                                    4, 1, BUTTON_CONFIG_START
                            ),
                            SecondaryDialConfig.Cross(
                                    8,
                                    2.2f,
                                    MOTION_SOURCE_RIGHT_STICK,
                                    R.drawable.direction_alt_background,
                                    R.drawable.direction_alt_foreground,
                                    contentDescription = CrossContentDescription(
                                        baseName = "c"
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
                    )
            )

    val PSP_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                            SecondaryDialConfig.SingleButton(3, 2,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_L1,
                                            label = "L"
                                    )
                            ),
                            SecondaryDialConfig.Stick(9, 2.2f, MOTION_SOURCE_LEFT_STICK),
                            SecondaryDialConfig.Empty(8, 1, 1f)
                    )
            )

    val PSP_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            listOf(
                                    BUTTON_CONFIG_CIRCLE,
                                    BUTTON_CONFIG_TRIANGLE,
                                    BUTTON_CONFIG_SQUARE,
                                    BUTTON_CONFIG_CROSS
                            )
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(
                                    2, 2, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_R1,
                                    label = "R"
                            )
                            ),
                            SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_START),
                            SecondaryDialConfig.Empty(8, 2, 2.2f),
                            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
                    )
            )

    val SNES_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                            SecondaryDialConfig.SingleButton(3, 2,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_L1,
                                            label = "L"
                                    )
                            ),
                            SecondaryDialConfig.Empty(8, 1, 1f)
                    )
            )

    val SNES_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            dials = listOf(
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_A,
                                            label = "A"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_X,
                                            label = "X"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_Y,
                                            label = "Y"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_B,
                                            label = "B"
                                    )
                            )
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 2,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_R1,
                                            label = "R"
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_START),
                            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
                    )
            )

    val GBA_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                            SecondaryDialConfig.SingleButton(3, 2,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_L1,
                                            label = "L"
                                    )
                            ),
                            SecondaryDialConfig.Empty(8, 1, 1f)
                    )
            )

    val GBA_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            dials = listOf(
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_A,
                                            label = "A"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_B,
                                            label = "B"
                                    )
                            ),
                            rotationInDegrees = 30f
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 2,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_R1,
                                            label = "R"
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_START),
                            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
                    )
            )

    val GENESIS_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                            SecondaryDialConfig.Empty(8, 1, 1f)
                    )
            )

    val GENESIS_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            dials = listOf(
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_A,
                                            label = "C"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_R1,
                                            label = "Z"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_X,
                                            label = "Y"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_L1,
                                            label = "X"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_Y,
                                            label = "A"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_UNKNOWN,
                                            visible = false
                                    )
                            ),
                            center = ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_B,
                                    label = "B"
                            )
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
                    )
            )

    val ATARI2600_LEFT =
            RadialGamePadConfig(
                    sockets = 10,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(3, 1,
                                    ButtonConfig(
                                        id = KeyEvent.KEYCODE_BUTTON_L1,
                                        label = "DIFF.A"
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_L2,
                                            label = "DIFF.B"
                                    )
                            ),
                            SecondaryDialConfig.Empty(7, 1, 1f)
                    )
            )

    val ATARI2600_RIGHT =
            RadialGamePadConfig(
                    sockets = 10,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            dials = listOf(),
                            center = ButtonConfig(
                                id = KeyEvent.KEYCODE_BUTTON_B,
                                contentDescription = "Action"
                            )
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_START,
                                            label = "RESET"
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(3, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_SELECT,
                                            label = "SELECT"
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(8, 1, BUTTON_CONFIG_MENU)
                    )
            )

    val SMS_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.Empty(4, 1, 1f),
                            SecondaryDialConfig.Empty(8, 1, 1f)
                    )
            )

    val SMS_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            dials = listOf(
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_A,
                                            label = "2"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_B,
                                            label = "1"
                                    )
                            )
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
                    )
            )

    val GG_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.Empty(4, 1, 1f),
                            SecondaryDialConfig.Empty(8, 1, 1f)
                    )
            )

    val GG_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            dials = listOf(
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_A,
                                            label = "2"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_B,
                                            label = "1"
                                    )
                            ),
                            rotationInDegrees = 30f
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
                    )
            )

    val FBNEO_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD_AND_LEFT_STICK),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(
                                    4, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_SELECT,
                                    iconId = R.drawable.button_coin,
                                    contentDescription = "Coin"
                            )
                            ),
                            SecondaryDialConfig.Empty(8, 1, 1f)
                    )
            )

    val FBNEO_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            dials = listOf(
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_R1,
                                            contentDescription = "R1"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_L1,
                                            contentDescription = "L1"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_X,
                                            contentDescription = "X"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_Y,
                                            contentDescription = "Y"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_B,
                                            contentDescription = "B"
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_UNKNOWN,
                                            visible = false
                                    )
                            ),
                            center = ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_A
                            )
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
                    )
            )
}
