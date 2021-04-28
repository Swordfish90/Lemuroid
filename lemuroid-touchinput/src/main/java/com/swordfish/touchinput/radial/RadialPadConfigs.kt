package com.swordfish.touchinput.radial

import android.view.KeyEvent
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.PrimaryDialConfig
import com.swordfish.radialgamepad.library.config.SecondaryDialConfig
import com.swordfish.radialgamepad.library.config.CrossContentDescription
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.touchinput.controller.R

object RadialPadConfigs {

    const val MOTION_SOURCE_DPAD = 0
    const val MOTION_SOURCE_LEFT_STICK = 1
    const val MOTION_SOURCE_RIGHT_STICK = 2
    const val MOTION_SOURCE_DPAD_AND_LEFT_STICK = 3
    const val MOTION_SOURCE_RIGHT_DPAD = 4

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
        contentDescription = "Menu",
        supportsGestures = setOf(GestureType.FIRST_TOUCH),
        supportsButtons = false
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

    private val BUTTON_CONFIG_COIN = ButtonConfig(
        id = KeyEvent.KEYCODE_BUTTON_SELECT,
        iconId = R.drawable.button_coin,
        contentDescription = "Coin"
    )

    private val BUTTON_CONFIG_L = ButtonConfig(
        id = KeyEvent.KEYCODE_BUTTON_L1,
        label = "L",
        supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
    )

    private val BUTTON_CONFIG_R = ButtonConfig(
        id = KeyEvent.KEYCODE_BUTTON_R1,
        label = "R",
        supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
    )

    private val BUTTON_CONFIG_L1 = ButtonConfig(
        id = KeyEvent.KEYCODE_BUTTON_L1,
        label = "L1",
        supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
    )

    private val BUTTON_CONFIG_R1 = ButtonConfig(
        id = KeyEvent.KEYCODE_BUTTON_R1,
        label = "R1",
        supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
    )

    private val BUTTON_CONFIG_L2 = ButtonConfig(
        id = KeyEvent.KEYCODE_BUTTON_L2,
        label = "L2",
        supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
    )

    private val BUTTON_CONFIG_R2 = ButtonConfig(
        id = KeyEvent.KEYCODE_BUTTON_R2,
        label = "R2",
        supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
    )

    private val PRIMARY_DIAL_CROSS = PrimaryDialConfig.Cross(
        MOTION_SOURCE_DPAD,
        supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
    )

    private val PRIMARY_DIAL_CROSS_MERGED = PrimaryDialConfig.Cross(
        MOTION_SOURCE_DPAD_AND_LEFT_STICK,
        supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
    )

    val GB_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
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
            primaryDial = PRIMARY_DIAL_CROSS,
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

    val DESMUME_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    8,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_THUMBL,
                        iconId = R.drawable.button_mic,
                        contentDescription = "Microphone"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    10,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L2,
                        iconId = R.drawable.button_close_screen,
                        contentDescription = "Close"
                    )
                ),
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_L)
            )
        )

    val DESMUME_RIGHT = RadialGamePadConfig(
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
            SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_R),
            SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_START),
            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
        )
    )

    val MELONDS_NDS_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    8,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L2,
                        iconId = R.drawable.button_mic,
                        contentDescription = "Microphone"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    10,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_THUMBL,
                        iconId = R.drawable.button_close_screen,
                        contentDescription = "Close"
                    )
                ),
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_L)
            )
        )

    val MELONDS_NDS_RIGHT = RadialGamePadConfig(
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
            SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_R),
            SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_START),
            SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
        )
    )

    val PSX_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 1, BUTTON_CONFIG_L1),
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_L2),
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
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_R2),
                SecondaryDialConfig.SingleButton(3, 1, BUTTON_CONFIG_R1),
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )

    val PSX_DUALSHOCK_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 1, BUTTON_CONFIG_L1),
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_L2),
                SecondaryDialConfig.Stick(
                    9,
                    2.2f,
                    MOTION_SOURCE_LEFT_STICK,
                    KeyEvent.KEYCODE_BUTTON_THUMBL,
                    contentDescription = "Left Stick",
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
                ),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    val PSX_DUALSHOCK_RIGHT =
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
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_R2),
                SecondaryDialConfig.SingleButton(3, 1, BUTTON_CONFIG_R1),
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.Stick(
                    8,
                    2.2f,
                    MOTION_SOURCE_RIGHT_STICK,
                    KeyEvent.KEYCODE_BUTTON_THUMBR,
                    contentDescription = "Right Stick",
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
                ),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )

    val N64_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(2, 1, 1f),
                SecondaryDialConfig.SingleButton(
                    2,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L2,
                        label = "Z"
                    )
                ),
                SecondaryDialConfig.SingleButton(3, 2, BUTTON_CONFIG_L),
                SecondaryDialConfig.Empty(8, 1, 1f),
                SecondaryDialConfig.Stick(
                    9,
                    2.2f,
                    MOTION_SOURCE_LEFT_STICK,
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
                )
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
                SecondaryDialConfig.SingleButton(2, 2, BUTTON_CONFIG_R),
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.Cross(
                    8,
                    2.2f,
                    MOTION_SOURCE_RIGHT_DPAD,
                    R.drawable.direction_alt_background,
                    R.drawable.direction_alt_foreground,
                    contentDescription = CrossContentDescription(
                        baseName = "c"
                    ),
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
                    distanceFromCenter = 0.8f,
                    diagonalRatio = 4
                ),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )

    val PSP_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 2, BUTTON_CONFIG_L),
                SecondaryDialConfig.Stick(
                    9,
                    2.2f,
                    MOTION_SOURCE_LEFT_STICK,
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
                ),
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
                SecondaryDialConfig.SingleButton(2, 2, BUTTON_CONFIG_R),
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.Empty(8, 2, 2.2f),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )

    val SNES_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 2, BUTTON_CONFIG_L),
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
                SecondaryDialConfig.SingleButton(2, 2, BUTTON_CONFIG_R),
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )

    val GBA_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 2, BUTTON_CONFIG_L),
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
                SecondaryDialConfig.SingleButton(2, 2, BUTTON_CONFIG_R),
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )

    val GENESIS_3_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(10, 1, 1f)
            )
        )

    val GENESIS_3_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                dials = listOf(
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_A,
                        label = "C"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_B,
                        label = "B"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_Y,
                        label = "A"
                    ),
                ),
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )

    val GENESIS_6_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.SingleButton(8, 1, BUTTON_CONFIG_MENU),
                SecondaryDialConfig.Empty(9, 1, 1f)
            )
        )

    val GENESIS_6_RIGHT =
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
                SecondaryDialConfig.Empty(9, 1, 0.5f),
                SecondaryDialConfig.Empty(3, 1, 0.5f)
            )
        )

    val ATARI2600_LEFT =
        RadialGamePadConfig(
            sockets = 10,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    3,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L1,
                        label = "DIFF.A"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    2,
                    1,
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
                SecondaryDialConfig.SingleButton(
                    2,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_START,
                        label = "RESET"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    3,
                    1,
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
            primaryDial = PRIMARY_DIAL_CROSS,
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
            primaryDial = PRIMARY_DIAL_CROSS,
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

    val ARCADE_4_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS_MERGED,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_COIN),
                SecondaryDialConfig.Empty(10, 1, 1f)
            )
        )

    val ARCADE_4_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                rotationInDegrees = 60f,
                dials = listOf(
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_X,
                        contentDescription = "X",
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_Y,
                        contentDescription = "Y",
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_B,
                        contentDescription = "B",
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_A,
                        contentDescription = "A",
                    )
                ),
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )

    val ARCADE_6_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS_MERGED,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_COIN),
                SecondaryDialConfig.SingleButton(3, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.SingleButton(8, 1, BUTTON_CONFIG_MENU),
                SecondaryDialConfig.Empty(9, 1, 1f)
            )
        )

    val ARCADE_6_RIGHT =
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
                SecondaryDialConfig.Empty(9, 1, 0.5f),
                SecondaryDialConfig.Empty(3, 1, 0.5f)
            )
        )

    val LYNX_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    4,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L1,
                        label = "OPTION 1"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    2,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_R1,
                        label = "OPTION 2"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    8,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_SELECT,
                        iconId = R.drawable.button_rotate,
                        contentDescription = "Rotate"
                    )
                ),
                SecondaryDialConfig.Empty(10, 1, 1f)
            )
        )

    val LYNX_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                rotationInDegrees = 15f,
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
                SecondaryDialConfig.Empty(4, 1, 1f),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )

    val ATARI7800_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(10, 1, 1f)
            )
        )

    val ATARI7800_RIGHT =
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

    val PCE_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(10, 1, 1f)
            )
        )

    val PCE_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                dials = listOf(
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_A,
                        label = "II"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_B,
                        label = "I"
                    )
                )
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )

    val DOS_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_SELECT,
                        contentDescription = "Select"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    3,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L1,
                        contentDescription = "L1"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    4,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L2,
                        contentDescription = "L2"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    8,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_THUMBL,
                        iconId = R.drawable.button_keyboard,
                        contentDescription = "Keyboard"
                    )
                ),
                SecondaryDialConfig.Stick(
                    9,
                    2.2f,
                    MOTION_SOURCE_LEFT_STICK,
                    contentDescription = "Left Stick"
                ),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    val DOS_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                listOf(
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_A,
                        contentDescription = "A"
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
                    )
                )
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_R2,
                        contentDescription = "R2"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    3,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_R1,
                        contentDescription = "R1"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    4,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_START,
                        contentDescription = "Start"
                    )
                ),
                SecondaryDialConfig.Stick(
                    8,
                    2.2f,
                    MOTION_SOURCE_RIGHT_STICK,
                    contentDescription = "Right Stick"
                ),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )

    val NGP_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.Cross(MOTION_SOURCE_DPAD),
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(4, 1, 1f),
                SecondaryDialConfig.Empty(10, 1, 1f)
            )
        )

    val NGP_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                dials = listOf(
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_A,
                        label = "B"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_B,
                        label = "A"
                    )
                )
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                SecondaryDialConfig.SingleButton(10, 1, BUTTON_CONFIG_MENU)
            )
        )
}
