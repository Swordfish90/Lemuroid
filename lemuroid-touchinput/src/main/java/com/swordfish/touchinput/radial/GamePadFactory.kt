package com.swordfish.touchinput.radial

import android.content.Context
import com.swordfish.lemuroid.lib.library.SystemID

object GamePadFactory {
    fun createRadialGamePad(context: Context, systemId: SystemID): LemuroidVirtualGamePad {
        return when (systemId) {
            SystemID.SNES ->
                LemuroidVirtualGamePad(RadialPadConfigs.SNES_LEFT, RadialPadConfigs.SNES_RIGHT, context)

            SystemID.GBA ->
                LemuroidVirtualGamePad(RadialPadConfigs.GBA_LEFT, RadialPadConfigs.GBA_RIGHT, context)

            SystemID.GENESIS ->
                LemuroidVirtualGamePad(RadialPadConfigs.GENESIS_LEFT, RadialPadConfigs.GENESIS_RIGHT, context, 1.1f)

            SystemID.ATARI2600 ->
                LemuroidVirtualGamePad(RadialPadConfigs.ATARI2600_LEFT, RadialPadConfigs.ATARI2600_RIGHT, context)

            SystemID.SMS ->
                LemuroidVirtualGamePad(RadialPadConfigs.SMS_LEFT, RadialPadConfigs.SMS_RIGHT, context)

            SystemID.GG ->
                LemuroidVirtualGamePad(RadialPadConfigs.GG_LEFT, RadialPadConfigs.GG_RIGHT, context)

            SystemID.FBNEO ->
                LemuroidVirtualGamePad(RadialPadConfigs.FBNEO_LEFT, RadialPadConfigs.FBNEO_RIGHT, context, 1.1f)

            SystemID.GBC ->
                LemuroidVirtualGamePad(RadialPadConfigs.GB_LEFT, RadialPadConfigs.GB_RIGHT, context)

            SystemID.PSP ->
                LemuroidVirtualGamePad(RadialPadConfigs.PSP_LEFT, RadialPadConfigs.PSP_RIGHT, context, 1.1f)

            SystemID.N64 ->
                LemuroidVirtualGamePad(RadialPadConfigs.N64_LEFT, RadialPadConfigs.N64_RIGHT, context)

            SystemID.PSX ->
                LemuroidVirtualGamePad(RadialPadConfigs.PSX_LEFT, RadialPadConfigs.PSX_RIGHT, context, 1.1f)

            SystemID.NDS ->
                LemuroidVirtualGamePad(RadialPadConfigs.NDS_LEFT, RadialPadConfigs.NDS_RIGHT, context)

            SystemID.NES ->
                LemuroidVirtualGamePad(RadialPadConfigs.NES_LEFT, RadialPadConfigs.NES_RIGHT, context)

            SystemID.GB ->
                LemuroidVirtualGamePad(RadialPadConfigs.GB_LEFT, RadialPadConfigs.GB_RIGHT, context)
        }
    }
}
