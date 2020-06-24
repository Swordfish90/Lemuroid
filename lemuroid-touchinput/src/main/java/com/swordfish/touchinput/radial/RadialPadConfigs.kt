package com.swordfish.touchinput.radial

import android.view.KeyEvent
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.PrimaryDialConfig
import com.swordfish.radialgamepad.library.config.SecondaryDialConfig
import com.swordfish.touchinput.controller.R

object RadialPadConfigs {

    val GB_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(0),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(
                                    4, 1, ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_SELECT,
                                            iconId = R.drawable.button_select
                                    )
                            ),
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
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                        id = KeyEvent.KEYCODE_BUTTON_START,
                                        iconId = R.drawable.button_start
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(10, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )

    val NES_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(0),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(
                                    4, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_SELECT,
                                    iconId = R.drawable.button_select
                            )
                            ),
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
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_START,
                                            iconId = R.drawable.button_start
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(10, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )

    val NDS_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(0),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_THUMBL,
                                            iconId = R.drawable.button_blow
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(3, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_SELECT,
                                            iconId = R.drawable.button_select
                                    )
                            ),
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
                    SecondaryDialConfig.SingleButton(3, 1,
                            ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_START,
                                    iconId = R.drawable.button_start
                            )
                    ),
                    SecondaryDialConfig.SingleButton(4, 1,
                            ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_MODE,
                                    iconId = R.drawable.button_menu
                            )
                    )
            )
    )

    val PSX_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(GLRetroView.MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(
                                    2, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_SELECT,
                                    iconId = R.drawable.button_select
                            )
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
                                    GLRetroView.MOTION_SOURCE_ANALOG_LEFT,
                                    KeyEvent.KEYCODE_BUTTON_THUMBL
                            ),
                            SecondaryDialConfig.Empty(8, 1, 1f)
                    )
            )

    val PSX_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            listOf(
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_A,
                                            iconId = R.drawable.psx_circle
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_X,
                                            iconId = R.drawable.psx_triangle
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_Y,
                                            iconId = R.drawable.psx_square
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_B,
                                            iconId = R.drawable.psx_cross
                                    )
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
                                    4, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_START,
                                    iconId = R.drawable.button_start
                            )
                            ),
                            SecondaryDialConfig.Stick(
                                    8,
                                    2.2f,
                                    GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,
                                    KeyEvent.KEYCODE_BUTTON_THUMBR
                            ),
                            SecondaryDialConfig.SingleButton(10, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )

    val N64_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(GLRetroView.MOTION_SOURCE_DPAD),
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
                            SecondaryDialConfig.Empty(4, 1, 1f),
                            SecondaryDialConfig.Empty(8, 1, 1f),
                            SecondaryDialConfig.Stick(9, 2.2f, GLRetroView.MOTION_SOURCE_ANALOG_LEFT)
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
                            SecondaryDialConfig.Empty(2, 1, 1f),
                            SecondaryDialConfig.SingleButton(
                                    2, 2, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_R1,
                                    label = "R"
                            )
                            ),
                            SecondaryDialConfig.SingleButton(
                                    4, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_START,
                                    iconId = R.drawable.button_start
                            )
                            ),
                            SecondaryDialConfig.Cross(
                                    8,
                                    2.2f,
                                    GLRetroView.MOTION_SOURCE_ANALOG_RIGHT,
                                    R.drawable.direction_alt_background,
                                    R.drawable.direction_alt_foreground
                            ),
                            SecondaryDialConfig.SingleButton(10, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )

    val PSP_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(GLRetroView.MOTION_SOURCE_DPAD),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_SELECT,
                                            iconId = R.drawable.button_select
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(3, 2,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_L1,
                                            label = "L"
                                    )
                            ),
                            // This is a workaround for a RadialGamePad issue. Spread dials and rotation do not play well.
                            SecondaryDialConfig.Empty(4, 1, 1f),
                            SecondaryDialConfig.Stick(9, 2.2f, GLRetroView.MOTION_SOURCE_ANALOG_LEFT),
                            SecondaryDialConfig.Empty(8, 1, 1f)
                    )
            )

    val PSP_RIGHT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.PrimaryButtons(
                            listOf(
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_A,
                                            iconId = R.drawable.psx_circle
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_X,
                                            iconId = R.drawable.psx_triangle
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_Y,
                                            iconId = R.drawable.psx_square
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_B,
                                            iconId = R.drawable.psx_cross
                                    )
                            )
                    ),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(
                                    2, 2, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_R1,
                                    label = "R"
                            )
                            ),
                            // This is a workaround for a RadialGamePad issue. Spread dials and rotation do not play well.
                            SecondaryDialConfig.Empty(2, 1, 1f),
                            SecondaryDialConfig.SingleButton(4, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_START,
                                            iconId = R.drawable.button_start
                                    )
                            ),
                            SecondaryDialConfig.Empty(8, 2, 2.2f),
                            SecondaryDialConfig.SingleButton(10, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )

    val SNES_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(0),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_SELECT,
                                            iconId = R.drawable.button_select
                                    )
                            ),
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
                            SecondaryDialConfig.SingleButton(4, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_START,
                                            iconId = R.drawable.button_start
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(10, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )

    val GBA_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(0),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_SELECT,
                                            iconId = R.drawable.button_select
                                    )
                            ),
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
                            SecondaryDialConfig.SingleButton(4, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_START,
                                            iconId = R.drawable.button_start
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(10, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )

    val GENESIS_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(0),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(
                                    4, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_SELECT,
                                    iconId = R.drawable.button_select
                            )
                            ),
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
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_START,
                                            iconId = R.drawable.button_start
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(10, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )

    val ATARI2600_LEFT =
            RadialGamePadConfig(
                    sockets = 10,
                    primaryDial = PrimaryDialConfig.Cross(0),
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
                                id = KeyEvent.KEYCODE_BUTTON_B
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
                            SecondaryDialConfig.SingleButton(8, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )

    val SMS_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(0),
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
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_START,
                                            iconId = R.drawable.button_start
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(10, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )

    val GG_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(0),
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
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_START,
                                            iconId = R.drawable.button_start
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(10, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )

    val FBNEO_LEFT =
            RadialGamePadConfig(
                    sockets = 12,
                    primaryDial = PrimaryDialConfig.Cross(0),
                    secondaryDials = listOf(
                            SecondaryDialConfig.SingleButton(
                                    4, 1, ButtonConfig(
                                    id = KeyEvent.KEYCODE_BUTTON_SELECT,
                                    iconId = R.drawable.button_coin
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
                                            id = KeyEvent.KEYCODE_BUTTON_R1
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_L1
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_X
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_Y
                                    ),
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_B
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
                            SecondaryDialConfig.SingleButton(2, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_START,
                                            iconId = R.drawable.button_start
                                    )
                            ),
                            SecondaryDialConfig.SingleButton(10, 1,
                                    ButtonConfig(
                                            id = KeyEvent.KEYCODE_BUTTON_MODE,
                                            iconId = R.drawable.button_menu
                                    )
                            )
                    )
            )
}
