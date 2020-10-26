package com.swordfish.touchinput.radial

import android.content.Context
import com.swordfish.lemuroid.lib.library.SystemID

object GamePadFactory {
    fun createRadialGamePad(context: Context, systemId: SystemID, vibrateOnTouch: Boolean): LemuroidVirtualGamePad {
        return when (systemId) {
            SystemID.SNES -> LemuroidVirtualGamePad(
                RadialPadConfigs.SNES_LEFT,
                RadialPadConfigs.SNES_RIGHT,
                context,
                vibrateOnTouch
            )

            SystemID.GBA -> LemuroidVirtualGamePad(
                RadialPadConfigs.GBA_LEFT,
                RadialPadConfigs.GBA_RIGHT,
                context,
                vibrateOnTouch
            )

            SystemID.GENESIS -> LemuroidVirtualGamePad(
                RadialPadConfigs.GENESIS_LEFT,
                RadialPadConfigs.GENESIS_RIGHT,
                context,
                vibrateOnTouch,
                0.9f,
                1.1f
            )

            SystemID.ATARI2600 -> LemuroidVirtualGamePad(
                RadialPadConfigs.ATARI2600_LEFT,
                RadialPadConfigs.ATARI2600_RIGHT,
                context,
                vibrateOnTouch
            )

            SystemID.SMS -> LemuroidVirtualGamePad(
                RadialPadConfigs.SMS_LEFT,
                RadialPadConfigs.SMS_RIGHT,
                context,
                vibrateOnTouch
            )

            SystemID.GG -> LemuroidVirtualGamePad(
                RadialPadConfigs.GG_LEFT,
                RadialPadConfigs.GG_RIGHT,
                context,
                vibrateOnTouch
            )

            SystemID.FBNEO -> LemuroidVirtualGamePad(
                RadialPadConfigs.FBNEO_LEFT,
                RadialPadConfigs.FBNEO_RIGHT,
                context,
                vibrateOnTouch,
                0.9f,
                1.1f
            )

            // TODO FILIPPO... This has to be fixed.
            SystemID.MAME2003PLUS -> LemuroidVirtualGamePad(
                RadialPadConfigs.FBNEO_LEFT,
                RadialPadConfigs.FBNEO_RIGHT,
                context,
                vibrateOnTouch,
                0.9f,
                1.1f
            )

            // TODO FILIPPO... This has to be fixed.
            SystemID.MAME2000 -> LemuroidVirtualGamePad(
                RadialPadConfigs.FBNEO_LEFT,
                RadialPadConfigs.FBNEO_RIGHT,
                context,
                vibrateOnTouch,
                0.9f,
                1.1f
            )

            SystemID.GBC -> LemuroidVirtualGamePad(
                RadialPadConfigs.GB_LEFT,
                RadialPadConfigs.GB_RIGHT,
                context,
                vibrateOnTouch
            )

            SystemID.PSP -> LemuroidVirtualGamePad(
                RadialPadConfigs.PSP_LEFT,
                RadialPadConfigs.PSP_RIGHT,
                context,
                vibrateOnTouch,
                1.1f
            )

            SystemID.N64 -> LemuroidVirtualGamePad(
                RadialPadConfigs.N64_LEFT,
                RadialPadConfigs.N64_RIGHT,
                context,
                vibrateOnTouch
            )

            SystemID.PSX -> LemuroidVirtualGamePad(
                RadialPadConfigs.PSX_LEFT,
                RadialPadConfigs.PSX_RIGHT,
                context,
                vibrateOnTouch,
                1.1f
            )

            SystemID.NDS -> LemuroidVirtualGamePad(
                RadialPadConfigs.NDS_LEFT,
                RadialPadConfigs.NDS_RIGHT,
                context,
                vibrateOnTouch
            )

            SystemID.NES -> LemuroidVirtualGamePad(
                RadialPadConfigs.NES_LEFT,
                RadialPadConfigs.NES_RIGHT,
                context,
                vibrateOnTouch
            )

            SystemID.GB -> LemuroidVirtualGamePad(
                RadialPadConfigs.GB_LEFT,
                RadialPadConfigs.GB_RIGHT,
                context,
                vibrateOnTouch
            )
        }
    }
}
