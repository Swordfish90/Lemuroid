package com.swordfish.libretrodroid

import android.view.KeyEvent
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.CrossConfig
import com.swordfish.radialgamepad.library.config.PrimaryDialConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.SecondaryDialConfig

object VirtualGamePadConfigs {
    val RETRO_PAD_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.Cross(CrossConfig(0)),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_SELECT,
                        label = "SELECT"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    3,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L1,
                        label = "L1"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    4,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L2,
                        label = "L2"
                    )
                ),
                SecondaryDialConfig.Empty(
                    8,
                    1,
                    1f,
                    0f
                ),
                // When this stick is double tapped, it's going to fire a Button event
                SecondaryDialConfig.Stick(
                    9,
                    2,
                    2.2f,
                    0.1f,
                    1,
                    KeyEvent.KEYCODE_BUTTON_THUMBL,
                    contentDescription = "Left Stick",
                    rotationProcessor = object : SecondaryDialConfig.RotationProcessor() {
                        override fun getRotation(rotation: Float): Float {
                            return rotation - 10f
                        }
                    }
                )
            )
        )

    val RETRO_PAD_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                listOf(
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_A,
                        label = "A",
                        contentDescription = "Circle"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_X,
                        label = "X",
                        contentDescription = "Triangle"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_Y,
                        label = "Y",
                        contentDescription = "Square"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_B,
                        label = "B",
                        contentDescription = "Cross"
                    )
                )
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.DoubleButton(
                    2,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_R1,
                        label = "R"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    4,
                    1f,
                    0f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_START,
                        label = "START"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    10,
                    1f,
                    -0.1f,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_MODE,
                        label = "MENU"
                    )
                ),
                // When this stick is double tapped, it's going to fire a Button event
                SecondaryDialConfig.Cross(
                    8,
                    2,
                    2.2f,
                    0.1f,
                    CrossConfig(0),
                    rotationProcessor = object : SecondaryDialConfig.RotationProcessor() {
                        override fun getRotation(rotation: Float): Float {
                            return rotation + 8f
                        }
                    }
                )
            )
        )
}