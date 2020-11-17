package com.swordfish.touchinput.radial

import android.content.Context
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.SystemID
import java.lang.UnsupportedOperationException

object GamePadFactory {

    fun createRadialGamePad(
        context: Context,
        systemId: SystemID,
        coreID: CoreID,
        vibrateOnTouch: Boolean
    ): LemuroidVirtualGamePad {

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
                1f,
                1.2f
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

            SystemID.FBNEO, SystemID.MAME2003PLUS -> {
                LemuroidVirtualGamePad(
                    RadialPadConfigs.ARCADE_LEFT,
                    RadialPadConfigs.ARCADE_RIGHT,
                    context,
                    vibrateOnTouch,
                    1.0f,
                    1.2f
                )
            }

            SystemID.GB, SystemID.GBC -> LemuroidVirtualGamePad(
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
                1.1f,
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
                1.1f,
                1.1f
            )

            SystemID.NDS -> when (coreID) {
                CoreID.DESMUME -> LemuroidVirtualGamePad(
                    RadialPadConfigs.DESMUME_LEFT,
                    RadialPadConfigs.DESMUME_RIGHT,
                    context,
                    vibrateOnTouch
                )
                CoreID.MELONDS -> LemuroidVirtualGamePad(
                    RadialPadConfigs.MELONDS_NDS_LEFT,
                    RadialPadConfigs.MELONDS_NDS_RIGHT,
                    context,
                    vibrateOnTouch
                )
                else -> throw UnsupportedOperationException("This core is not supported on the system.")
            }

            SystemID.NES -> LemuroidVirtualGamePad(
                RadialPadConfigs.NES_LEFT,
                RadialPadConfigs.NES_RIGHT,
                context,
                vibrateOnTouch
            )
        }
    }
}
