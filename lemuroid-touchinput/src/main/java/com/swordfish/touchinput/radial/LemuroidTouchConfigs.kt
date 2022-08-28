package com.swordfish.touchinput.radial

import android.view.KeyEvent
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.CrossConfig
import com.swordfish.radialgamepad.library.config.CrossContentDescription
import com.swordfish.radialgamepad.library.config.PrimaryDialConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.config.SecondaryDialConfig
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.HapticConfig
import com.swordfish.touchinput.controller.R

object LemuroidTouchConfigs {

    private const val DEFAULT_STICK_ROTATION = 8f

    enum class Kind {
        GB_LEFT,
        GB_RIGHT,
        NES_LEFT,
        NES_RIGHT,
        DESMUME_LEFT,
        DESMUME_RIGHT,
        MELONDS_NDS_LEFT,
        MELONDS_NDS_RIGHT,
        PSX_LEFT,
        PSX_RIGHT,
        PSX_DUALSHOCK_LEFT,
        PSX_DUALSHOCK_RIGHT,
        PSP_LEFT,
        PSP_RIGHT,
        SNES_LEFT,
        SNES_RIGHT,
        GBA_LEFT,
        GBA_RIGHT,
        SMS_LEFT,
        SMS_RIGHT,
        GG_LEFT,
        GG_RIGHT,
        LYNX_LEFT,
        LYNX_RIGHT,
        PCE_LEFT,
        PCE_RIGHT,
        DOS_LEFT,
        DOS_RIGHT,
        NGP_LEFT,
        NGP_RIGHT,
        WS_LANDSCAPE_LEFT,
        WS_LANDSCAPE_RIGHT,
        WS_PORTRAIT_LEFT,
        WS_PORTRAIT_RIGHT,
        N64_LEFT,
        N64_RIGHT,
        GENESIS_3_LEFT,
        GENESIS_3_RIGHT,
        GENESIS_6_LEFT,
        GENESIS_6_RIGHT,
        ATARI2600_LEFT,
        ATARI2600_RIGHT,
        ARCADE_4_LEFT,
        ARCADE_4_RIGHT,
        ARCADE_6_LEFT,
        ARCADE_6_RIGHT,
        ATARI7800_LEFT,
        ATARI7800_RIGHT,
        NINTENDO_3DS_LEFT,
        NINTENDO_3DS_RIGHT
    }

    fun getRadialGamePadConfig(
        kind: Kind,
        haptic: HapticConfig,
        theme: RadialGamePadTheme
    ): RadialGamePadConfig {
        val radialGamePadConfig = when (kind) {
            Kind.GB_LEFT -> getGBLeft(theme)
            Kind.GB_RIGHT -> getGBRight(theme)
            Kind.NES_LEFT -> getNESLeft(theme)
            Kind.NES_RIGHT -> getNESRight(theme)
            Kind.DESMUME_LEFT -> getDesmumeLeft(theme)
            Kind.DESMUME_RIGHT -> getDesmumeRight(theme)
            Kind.MELONDS_NDS_LEFT -> getMelondsLeft(theme)
            Kind.MELONDS_NDS_RIGHT -> getMelondsRight(theme)
            Kind.PSX_LEFT -> getPSXLeft(theme)
            Kind.PSX_RIGHT -> getPSXRight(theme)
            Kind.PSX_DUALSHOCK_LEFT -> getPSXDualshockLeft(theme)
            Kind.PSX_DUALSHOCK_RIGHT -> getPSXDualshockRight(theme)
            Kind.PSP_LEFT -> getPSPLeft(theme)
            Kind.PSP_RIGHT -> getPSPRight(theme)
            Kind.SNES_LEFT -> getSNESLeft(theme)
            Kind.SNES_RIGHT -> getSNESRight(theme)
            Kind.GBA_LEFT -> getGBALeft(theme)
            Kind.GBA_RIGHT -> getGBARight(theme)
            Kind.SMS_LEFT -> getSMSLeft(theme)
            Kind.SMS_RIGHT -> getSMSRight(theme)
            Kind.GG_LEFT -> getGGLeft(theme)
            Kind.GG_RIGHT -> getGGRight(theme)
            Kind.LYNX_LEFT -> getLynxLeft(theme)
            Kind.LYNX_RIGHT -> getLynxRight(theme)
            Kind.PCE_LEFT -> getPCELeft(theme)
            Kind.PCE_RIGHT -> getPCERight(theme)
            Kind.DOS_LEFT -> getDOSLeft(theme)
            Kind.DOS_RIGHT -> getDOSRight(theme)
            Kind.NGP_LEFT -> getNGPLeft(theme)
            Kind.NGP_RIGHT -> getNGPRight(theme)
            Kind.WS_LANDSCAPE_LEFT -> getWSLandscapeLeft(theme)
            Kind.WS_LANDSCAPE_RIGHT -> getWSLandscapeRight(theme)
            Kind.WS_PORTRAIT_LEFT -> getWSPortraitLeft(theme)
            Kind.WS_PORTRAIT_RIGHT -> getWSPortraitRight(theme)
            Kind.N64_LEFT -> getN64Left(theme)
            Kind.N64_RIGHT -> getN64Right(theme)
            Kind.GENESIS_3_LEFT -> getGenesis3Left(theme)
            Kind.GENESIS_3_RIGHT -> getGenesis3Right(theme)
            Kind.GENESIS_6_LEFT -> getGenesis6Left(theme)
            Kind.GENESIS_6_RIGHT -> getGenesis6Right(theme)
            Kind.ATARI2600_LEFT -> getAtari2600Left(theme)
            Kind.ATARI2600_RIGHT -> getAtari2600Right(theme)
            Kind.ARCADE_4_LEFT -> getArcade4Left(theme)
            Kind.ARCADE_4_RIGHT -> getArcade4Right(theme)
            Kind.ARCADE_6_LEFT -> getArcade6Left(theme)
            Kind.ARCADE_6_RIGHT -> getArcade6Right(theme)
            Kind.ATARI7800_LEFT -> getAtari7800Left(theme)
            Kind.ATARI7800_RIGHT -> getAtari7800Right(theme)
            Kind.NINTENDO_3DS_LEFT -> getNintendo3DSLeft(theme)
            Kind.NINTENDO_3DS_RIGHT -> getNintendo3DSRight(theme)
        }

        return radialGamePadConfig.copy(haptic = haptic)
    }

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
        CrossConfig(
            id = MOTION_SOURCE_DPAD,
            shape = CrossConfig.Shape.CIRCLE,
            supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
            rightDrawableForegroundId = R.drawable.direction_alt_foreground
        ),
    )

    private val PRIMARY_DIAL_CROSS_MERGED = PrimaryDialConfig.Cross(
        CrossConfig(
            id = MOTION_SOURCE_DPAD_AND_LEFT_STICK,
            shape = CrossConfig.Shape.CIRCLE,
            supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
            rightDrawableForegroundId = R.drawable.direction_alt_foreground
        )
    )

    private fun getGBLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getGBRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getNESLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getNESRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getDesmumeLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    8,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_THUMBL,
                        iconId = R.drawable.button_mic,
                        contentDescription = "Microphone"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    10,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L2,
                        iconId = R.drawable.button_close_screen,
                        contentDescription = "Close"
                    )
                ),
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_L)
            )
        )

    private fun getDesmumeRight(theme: RadialGamePadTheme) = RadialGamePadConfig(
        theme = theme,
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
            SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_R),
            SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_START),
            buildMenuButtonConfig(10, theme)
        )
    )

    private fun getMelondsLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    8,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L2,
                        iconId = R.drawable.button_mic,
                        contentDescription = "Microphone"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    10,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_THUMBL,
                        iconId = R.drawable.button_close_screen,
                        contentDescription = "Close"
                    )
                ),
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_L)
            )
        )

    private fun getMelondsRight(theme: RadialGamePadTheme) = RadialGamePadConfig(
        theme = theme,
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
            SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_R),
            SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_START),
            buildMenuButtonConfig(10, theme)
        )
    )

    private fun getPSXLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 1f, 0f, BUTTON_CONFIG_L1),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_L2),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getPSXRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_R2),
                SecondaryDialConfig.SingleButton(3, 1f, 0f, BUTTON_CONFIG_R1),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getPSXDualshockLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 1f, 0f, BUTTON_CONFIG_L1),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_L2),
                SecondaryDialConfig.Stick(
                    9,
                    2,
                    2.2f,
                    0f,
                    MOTION_SOURCE_LEFT_STICK,
                    KeyEvent.KEYCODE_BUTTON_THUMBL,
                    contentDescription = "Left Stick",
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
                    rotationProcessor = rotationOffset(-DEFAULT_STICK_ROTATION)
                ),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getPSXDualshockRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_R2),
                SecondaryDialConfig.SingleButton(3, 1f, 0f, BUTTON_CONFIG_R1),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme),
                SecondaryDialConfig.Stick(
                    8,
                    2,
                    2.2f,
                    0f,
                    MOTION_SOURCE_RIGHT_STICK,
                    KeyEvent.KEYCODE_BUTTON_THUMBR,
                    contentDescription = "Right Stick",
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
                    rotationProcessor = rotationOffset(DEFAULT_STICK_ROTATION)
                )
            )
        )

    private fun getN64Left(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L2,
                        label = "Z"
                    )
                ),
                SecondaryDialConfig.DoubleButton(3, 0f, BUTTON_CONFIG_L),
                SecondaryDialConfig.Stick(
                    9,
                    2,
                    2.2f,
                    0.1f,
                    MOTION_SOURCE_LEFT_STICK,
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
                    rotationProcessor = rotationOffset(-DEFAULT_STICK_ROTATION)
                ),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getN64Right(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.DoubleButton(2, 0f, BUTTON_CONFIG_R),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_START),
                SecondaryDialConfig.SingleButton(
                    index = 10,
                    scale = 1f,
                    distance = -0.1f,
                    buttonConfig = BUTTON_CONFIG_MENU,
                    rotationProcessor = object : SecondaryDialConfig.RotationProcessor() {
                        override fun getRotation(rotation: Float): Float {
                            return -rotation
                        }
                    },
                    theme = theme
                ),
                SecondaryDialConfig.Cross(
                    8,
                    2,
                    2.2f,
                    0.1f,
                    CrossConfig(
                        id = MOTION_SOURCE_RIGHT_DPAD,
                        shape = CrossConfig.Shape.CIRCLE,
                        rightDrawableForegroundId = R.drawable.direction_alt_foreground,
                        contentDescription = CrossContentDescription(
                            baseName = "c"
                        ),
                        supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
                        useDiagonals = false,
                    ),
                    rotationProcessor = rotationOffset(DEFAULT_STICK_ROTATION)
                )
            )
        )

    private fun getPSPLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.DoubleButton(3, 0f, BUTTON_CONFIG_L),
                SecondaryDialConfig.Stick(
                    9,
                    2,
                    2.2f,
                    0f,
                    MOTION_SOURCE_LEFT_STICK,
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
                    rotationProcessor = rotationOffset(-DEFAULT_STICK_ROTATION)
                ),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getPSPRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.DoubleButton(2, 0f, BUTTON_CONFIG_R),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme),
                SecondaryDialConfig.Empty(
                    8,
                    2,
                    2.2f,
                    0f,
                    rotationProcessor = rotationOffset(DEFAULT_STICK_ROTATION)
                )
            )
        )

    private fun getSNESLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.DoubleButton(3, 0f, BUTTON_CONFIG_L),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getSNESRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.DoubleButton(2, 0f, BUTTON_CONFIG_R),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getGBALeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.DoubleButton(3, 0f, BUTTON_CONFIG_L),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getGBARight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.DoubleButton(2, 0f, BUTTON_CONFIG_R),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getGenesis3Left(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getGenesis3Right(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getGenesis6Left(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(8, theme),
                SecondaryDialConfig.Empty(9, 1, 1f, 0f)
            )
        )

    private fun getGenesis6Right(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.Empty(9, 1, 0.5f, 0f),
                SecondaryDialConfig.Empty(3, 1, 0.5f, 0f)
            )
        )

    private fun getAtari2600Left(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    4,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L1,
                        label = "DIFF.A"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    2,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L2,
                        label = "DIFF.B"
                    )
                ),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getAtari2600Right(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
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
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_START,
                        label = "RESET"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    4,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_SELECT,
                        label = "SELECT"
                    )
                ),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getSMSLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(4, 1, 1f, 0f),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getSMSRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getGGLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(4, 1, 1f, 0f),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getGGRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getArcade4Left(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS_MERGED,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_COIN),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getArcade4Right(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getArcade6Left(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS_MERGED,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_COIN),
                SecondaryDialConfig.SingleButton(3, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(8, theme),
                SecondaryDialConfig.Empty(9, 1, 1f, 0f)
            )
        )

    private fun getArcade6Right(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.Empty(9, 1, 0.5f, 0f),
                SecondaryDialConfig.Empty(3, 1, 0.5f, 0f)
            )
        )

    private fun getLynxLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    4,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L1,
                        label = "OPTION 1"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    8,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_R1,
                        label = "OPTION 2"
                    )
                ),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getLynxRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getAtari7800Left(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getAtari7800Right(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getPCELeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getPCERight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getDOSLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 1f, 0f, BUTTON_CONFIG_L1),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_L2),
                SecondaryDialConfig.SingleButton(
                    8,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_THUMBL,
                        iconId = R.drawable.button_keyboard,
                        contentDescription = "Keyboard"
                    ),
                    rotationProcessor = rotationInvert(),
                ),
                SecondaryDialConfig.Stick(
                    9,
                    2,
                    2.2f,
                    0f,
                    MOTION_SOURCE_LEFT_STICK,
                    contentDescription = "Left Stick",
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
                    rotationProcessor = rotationOffset(-DEFAULT_STICK_ROTATION)
                )
            )
        )

    private fun getDOSRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_R2),
                SecondaryDialConfig.SingleButton(3, 1f, 0f, BUTTON_CONFIG_R1),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme),
                SecondaryDialConfig.Stick(
                    8,
                    2,
                    2.2f,
                    0f,
                    MOTION_SOURCE_RIGHT_STICK,
                    KeyEvent.KEYCODE_BUTTON_THUMBR,
                    contentDescription = "Right Stick",
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
                    rotationProcessor = rotationOffset(DEFAULT_STICK_ROTATION)
                )
            )
        )

    private fun getNGPLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(4, 1, 1f, 0f),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getNGPRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getWSLandscapeLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(4, 1, 1f, 0f),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getWSLandscapeRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                rotationInDegrees = 30f,
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
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getWSPortraitLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(4, 1, 1f, 0f),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getWSPortraitRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                dials = listOf(
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_A,
                        label = "X3"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_X,
                        label = "X2"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_Y,
                        label = "X1"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_B,
                        label = "X4"
                    )
                )
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme)
            )
        )

    private fun getNintendo3DSLeft(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1f, 0f, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.DoubleButton(3, 0f, BUTTON_CONFIG_L),
                SecondaryDialConfig.Stick(
                    9,
                    2,
                    2.2f,
                    0f,
                    MOTION_SOURCE_LEFT_STICK,
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
                    rotationProcessor = rotationOffset(-DEFAULT_STICK_ROTATION)
                ),
                SecondaryDialConfig.Empty(8, 1, 1f, 0f)
            )
        )

    private fun getNintendo3DSRight(theme: RadialGamePadTheme) =
        RadialGamePadConfig(
            theme = theme,
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
                SecondaryDialConfig.DoubleButton(2, 0f, BUTTON_CONFIG_R),
                SecondaryDialConfig.SingleButton(4, 1f, 0f, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, theme),
                SecondaryDialConfig.Empty(
                    8,
                    2,
                    2.2f,
                    0f,
                    rotationProcessor = rotationOffset(DEFAULT_STICK_ROTATION)
                )
            )
        )

    private fun buildMenuButtonConfig(index: Int, theme: RadialGamePadTheme): SecondaryDialConfig {
        return SecondaryDialConfig.SingleButton(
            index = index,
            scale = 1f,
            distance = 0f,
            buttonConfig = BUTTON_CONFIG_MENU,
            rotationProcessor = rotationInvert(),
            theme = theme
        )
    }

    private fun rotationOffset(degrees: Float): SecondaryDialConfig.RotationProcessor {
        return object : SecondaryDialConfig.RotationProcessor() {
            override fun getRotation(rotation: Float): Float {
                return rotation + degrees
            }
        }
    }

    private fun rotationInvert() = object : SecondaryDialConfig.RotationProcessor() {
        override fun getRotation(rotation: Float): Float {
            return -rotation
        }
    }
}
