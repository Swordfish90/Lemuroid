package com.swordfish.touchinput.radial

import android.content.Context
import com.swordfish.lemuroid.lib.library.SystemID

object GamePadFactory {
    fun createRadialGamePad(context: Context, systemId: SystemID): TiltRadialGamePad {
        return when (systemId) {
            SystemID.SNES ->
                TiltRadialGamePad(RadialPadConfigs.SNES_LEFT, RadialPadConfigs.SNES_RIGHT, context)

            SystemID.GBA ->
                TiltRadialGamePad(RadialPadConfigs.GBA_LEFT, RadialPadConfigs.GBA_RIGHT, context)

            SystemID.GENESIS ->
                TiltRadialGamePad(RadialPadConfigs.GENESIS_LEFT, RadialPadConfigs.GENESIS_RIGHT, context)

            SystemID.ATARI2600 ->
                TiltRadialGamePad(RadialPadConfigs.ATARI2600_LEFT, RadialPadConfigs.ATARI2600_RIGHT, context)

            SystemID.SMS ->
                TiltRadialGamePad(RadialPadConfigs.SMS_LEFT, RadialPadConfigs.SMS_RIGHT, context)

            SystemID.GG ->
                TiltRadialGamePad(RadialPadConfigs.GG_LEFT, RadialPadConfigs.GG_RIGHT, context)

            SystemID.FBNEO ->
                TiltRadialGamePad(RadialPadConfigs.FBNEO_LEFT, RadialPadConfigs.FBNEO_RIGHT, context)

            SystemID.GBC ->
                TiltRadialGamePad(RadialPadConfigs.GB_LEFT, RadialPadConfigs.GB_RIGHT, context)

            SystemID.PSP ->
                TiltRadialGamePad(RadialPadConfigs.PSP_LEFT, RadialPadConfigs.PSP_RIGHT, context)

            SystemID.N64 ->
                TiltRadialGamePad(RadialPadConfigs.N64_LEFT, RadialPadConfigs.N64_RIGHT, context)

            SystemID.PSX ->
                TiltRadialGamePad(RadialPadConfigs.PSX_LEFT, RadialPadConfigs.PSX_RIGHT, context)

            SystemID.NDS ->
                TiltRadialGamePad(RadialPadConfigs.NDS_LEFT, RadialPadConfigs.NDS_RIGHT, context)

            SystemID.NES ->
                TiltRadialGamePad(RadialPadConfigs.NES_LEFT, RadialPadConfigs.NES_RIGHT, context)

            SystemID.GB ->
                TiltRadialGamePad(RadialPadConfigs.GB_LEFT, RadialPadConfigs.GB_RIGHT, context)
        }
    }
}
