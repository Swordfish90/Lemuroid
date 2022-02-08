package com.swordfish.touchinput.radial

import android.content.Context
import android.view.KeyEvent
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.CrossConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.PrimaryDialConfig
import com.swordfish.radialgamepad.library.config.SecondaryDialConfig
import com.swordfish.radialgamepad.library.config.CrossContentDescription
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.HapticConfig
import com.swordfish.touchinput.controller.R

object LemuroidTouchConfigs {

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
    }

    data class Config(
        val standardTheme: RadialGamePadTheme,
        val menuTheme: RadialGamePadTheme,
        val haptic: HapticConfig
    )

    fun getRadialGamePadConfig(
        kind: Kind,
        accentColor: Int,
        haptic: HapticConfig,
        context: Context
    ): RadialGamePadConfig {
        val config = Config(
            LemuroidTouchThemes.getGamePadTheme(accentColor, context),
            LemuroidTouchThemes.getMenuTheme(accentColor, context),
            haptic
        )
        return when (kind) {
            Kind.GB_LEFT -> getGBLeft(config)
            Kind.GB_RIGHT -> getGBRight(config)
            Kind.NES_LEFT -> getNESLeft(config)
            Kind.NES_RIGHT -> getNESRight(config)
            Kind.DESMUME_LEFT -> getDesmumeLeft(config)
            Kind.DESMUME_RIGHT -> getDesmumeRight(config)
            Kind.MELONDS_NDS_LEFT -> getMelondsLeft(config)
            Kind.MELONDS_NDS_RIGHT -> getMelondsRight(config)
            Kind.PSX_LEFT -> getPSXLeft(config)
            Kind.PSX_RIGHT -> getPSXRight(config)
            Kind.PSX_DUALSHOCK_LEFT -> getPSXDualshockLeft(config)
            Kind.PSX_DUALSHOCK_RIGHT -> getPSXDualshockRight(config)
            Kind.PSP_LEFT -> getPSPLeft(config)
            Kind.PSP_RIGHT -> getPSPRight(config)
            Kind.SNES_LEFT -> getSNESLeft(config)
            Kind.SNES_RIGHT -> getSNESRight(config)
            Kind.GBA_LEFT -> getGBALeft(config)
            Kind.GBA_RIGHT -> getGBARight(config)
            Kind.SMS_LEFT -> getSMSLeft(config)
            Kind.SMS_RIGHT -> getSMSRight(config)
            Kind.GG_LEFT -> getGGLeft(config)
            Kind.GG_RIGHT -> getGGRight(config)
            Kind.LYNX_LEFT -> getLynxLeft(config)
            Kind.LYNX_RIGHT -> getLynxRight(config)
            Kind.PCE_LEFT -> getPCELeft(config)
            Kind.PCE_RIGHT -> getPCERight(config)
            Kind.DOS_LEFT -> getDOSLeft(config)
            Kind.DOS_RIGHT -> getDOSRight(config)
            Kind.NGP_LEFT -> getNGPLeft(config)
            Kind.NGP_RIGHT -> getNGPRight(config)
            Kind.WS_LANDSCAPE_LEFT -> getWSLandscapeLeft(config)
            Kind.WS_LANDSCAPE_RIGHT -> getWSLandscapeRight(config)
            Kind.WS_PORTRAIT_LEFT -> getWSPortraitLeft(config)
            Kind.WS_PORTRAIT_RIGHT -> getWSPortraitRight(config)
            Kind.N64_LEFT -> getN64Left(config)
            Kind.N64_RIGHT -> getN64Right(config)
            Kind.GENESIS_3_LEFT -> getGenesis3Left(config)
            Kind.GENESIS_3_RIGHT -> getGenesis3Right(config)
            Kind.GENESIS_6_LEFT -> getGenesis6Left(config)
            Kind.GENESIS_6_RIGHT -> getGenesis6Right(config)
            Kind.ATARI2600_LEFT -> getAtari2600Left(config)
            Kind.ATARI2600_RIGHT -> getAtari2600Right(config)
            Kind.ARCADE_4_LEFT -> getArcade4Left(config)
            Kind.ARCADE_4_RIGHT -> getArcade4Right(config)
            Kind.ARCADE_6_LEFT -> getArcade6Left(config)
            Kind.ARCADE_6_RIGHT -> getArcade6Right(config)
            Kind.ATARI7800_LEFT -> getAtari7800Left(config)
            Kind.ATARI7800_RIGHT -> getAtari7800Right(config)
        }
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
            shape = CrossConfig.Shape.STANDARD,
            supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
        ),
    )

    private val PRIMARY_DIAL_CROSS_MERGED = PrimaryDialConfig.Cross(
        CrossConfig(
            id = MOTION_SOURCE_DPAD_AND_LEFT_STICK,
            shape = CrossConfig.Shape.STANDARD,
            supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
        )
    )

    private fun getGBLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getGBRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getNESLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getNESRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getDesmumeLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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

    private fun getDesmumeRight(config: Config) = RadialGamePadConfig(
        theme = config.standardTheme,
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
            buildMenuButtonConfig(10, config)
        )
    )

    private fun getMelondsLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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

    private fun getMelondsRight(config: Config) = RadialGamePadConfig(
        theme = config.standardTheme,
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
            buildMenuButtonConfig(10, config)
        )
    )

    private fun getPSXLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 1, BUTTON_CONFIG_L1),
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_L2),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getPSXRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getPSXDualshockLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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

    private fun getPSXDualshockRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getN64Left(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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

    private fun getN64Right(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                    CrossConfig(
                        id = MOTION_SOURCE_RIGHT_DPAD,
                        shape = CrossConfig.Shape.CIRCLE,
                        rightDrawableForegroundId = R.drawable.direction_alt_foreground,
                        contentDescription = CrossContentDescription(
                            baseName = "c"
                        ),
                        supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH),
                        useDiagonals = false
                    )
                ),
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getPSPLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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

    private fun getPSPRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getSNESLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 2, BUTTON_CONFIG_L),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getSNESRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getGBALeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 2, BUTTON_CONFIG_L),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getGBARight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getGenesis3Left(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getGenesis3Right(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getGenesis6Left(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.SingleButton(3, 1, BUTTON_CONFIG_START),
                buildMenuButtonConfig(8, config),
                SecondaryDialConfig.Empty(9, 1, 1f)
            )
        )

    private fun getGenesis6Right(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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

    private fun getAtari2600Left(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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

    private fun getAtari2600Right(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(8, config)
            )
        )

    private fun getSMSLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(4, 1, 1f),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getSMSRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getGGLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(4, 1, 1f),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getGGRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getArcade4Left(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS_MERGED,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_COIN),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getArcade4Right(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getArcade6Left(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS_MERGED,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_COIN),
                SecondaryDialConfig.SingleButton(3, 1, BUTTON_CONFIG_START),
                buildMenuButtonConfig(8, config),
                SecondaryDialConfig.Empty(9, 1, 1f)
            )
        )

    private fun getArcade6Right(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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

    private fun getLynxLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                    8,
                    1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_R1,
                        label = "OPTION 2"
                    )
                ),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getLynxRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getAtari7800Left(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getAtari7800Right(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getPCELeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(4, 1, BUTTON_CONFIG_SELECT),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getPCERight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getDOSLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
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
                    contentDescription = "Left Stick",
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
                ),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getDOSRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                    contentDescription = "Right Stick",
                    supportsGestures = setOf(GestureType.TRIPLE_TAP, GestureType.FIRST_TOUCH)
                ),
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getNGPLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(4, 1, 1f),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getNGPRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getWSLandscapeLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(4, 1, 1f),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getWSLandscapeRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, config)
            )
        )

    private fun getWSPortraitLeft(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
            sockets = 12,
            primaryDial = PRIMARY_DIAL_CROSS,
            secondaryDials = listOf(
                SecondaryDialConfig.Empty(4, 1, 1f),
                SecondaryDialConfig.Empty(8, 1, 1f)
            )
        )

    private fun getWSPortraitRight(config: Config) =
        RadialGamePadConfig(
            theme = config.standardTheme,
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
                SecondaryDialConfig.SingleButton(2, 1, BUTTON_CONFIG_START),
                buildMenuButtonConfig(10, config)
            )
        )

    private fun buildMenuButtonConfig(index: Int, config: Config): SecondaryDialConfig {
        return SecondaryDialConfig.SingleButton(
            index = index,
            spread = 1,
            buttonConfig = BUTTON_CONFIG_MENU,
            processSecondaryDialRotation = { -it },
            theme = config.menuTheme
        )
    }
}
