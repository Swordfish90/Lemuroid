package com.swordfish.touchinput.radial

import android.content.Context
import com.swordfish.lemuroid.lib.library.SystemID

object RadialPadFactory {
    fun createRadialGamePad(context: Context, systemId: SystemID): BaseRadialPad {
        return when (systemId) {
            SystemID.SNES -> BaseRadialPad(RadialPadConfigs.SNES_LEFT, RadialPadConfigs.SNES_RIGHT, context)
            SystemID.GBA -> BaseRadialPad(RadialPadConfigs.GBA_LEFT, RadialPadConfigs.GBA_RIGHT, context)
            SystemID.GENESIS -> BaseRadialPad(RadialPadConfigs.GENESIS_LEFT, RadialPadConfigs.GENESIS_RIGHT, context)
            SystemID.ATARI2600 -> BaseRadialPad(RadialPadConfigs.ATARI2600_LEFT, RadialPadConfigs.ATARI2600_RIGHT, context)
            SystemID.SMS -> BaseRadialPad(RadialPadConfigs.SMS_LEFT, RadialPadConfigs.SMS_RIGHT, context)
            SystemID.GG -> BaseRadialPad(RadialPadConfigs.GG_LEFT, RadialPadConfigs.GG_RIGHT, context)
            SystemID.FBNEO -> BaseRadialPad(RadialPadConfigs.FBNEO_LEFT, RadialPadConfigs.FBNEO_RIGHT, context)
            SystemID.GBC -> BaseRadialPad(RadialPadConfigs.GB_LEFT,  RadialPadConfigs.GB_RIGHT, context)
            SystemID.PSP -> BaseRadialPad(RadialPadConfigs.PSP_LEFT, RadialPadConfigs.PSP_RIGHT, context)
            SystemID.N64 -> BaseRadialPad(RadialPadConfigs.N64_LEFT, RadialPadConfigs.N64_RIGHT, context)
            SystemID.PSX -> BaseRadialPad(RadialPadConfigs.PSX_LEFT, RadialPadConfigs.PSX_RIGHT, context)
            SystemID.NDS -> BaseRadialPad(RadialPadConfigs.NDS_LEFT, RadialPadConfigs.NDS_RIGHT, context)
            SystemID.NES -> BaseRadialPad(RadialPadConfigs.NES_LEFT, RadialPadConfigs.NES_RIGHT, context)
            SystemID.GB -> BaseRadialPad(RadialPadConfigs.GB_LEFT, RadialPadConfigs.GB_RIGHT, context)
        }
    }
}
