package com.swordfish.lemuroid.lib.controller

import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.touchinput.radial.RadialPadConfigs

enum class TouchControllerID {
    GB,
    NES,
    DESMUME,
    MELONDS,
    PSX,
    PSX_DUALSHOCK,
    N64,
    PSP,
    SNES,
    GBA,
    GENESIS_3,
    GENESIS_6,
    ATARI2600,
    SMS,
    GG,
    ARCADE_4,
    ARCADE_6,
    LYNX,
    ATARI7800,
    PCE;

    class Config(
        val leftConfig: RadialGamePadConfig,
        val rightConfig: RadialGamePadConfig,
        val leftScale: Float = 1.0f,
        val rightScale: Float = 1.0f
    )

    companion object {
        fun getConfig(id: TouchControllerID): Config {
            return when (id) {
                GB -> Config(RadialPadConfigs.GB_LEFT, RadialPadConfigs.GB_RIGHT)
                NES -> Config(RadialPadConfigs.NES_LEFT, RadialPadConfigs.NES_RIGHT)
                DESMUME -> Config(RadialPadConfigs.DESMUME_LEFT, RadialPadConfigs.DESMUME_RIGHT)
                MELONDS -> Config(RadialPadConfigs.MELONDS_NDS_LEFT, RadialPadConfigs.MELONDS_NDS_RIGHT)
                PSX -> Config(RadialPadConfigs.PSX_LEFT, RadialPadConfigs.PSX_RIGHT)
                PSX_DUALSHOCK -> Config(RadialPadConfigs.PSX_DUALSHOCK_LEFT, RadialPadConfigs.PSX_DUALSHOCK_RIGHT)
                N64 -> Config(RadialPadConfigs.N64_LEFT, RadialPadConfigs.N64_RIGHT)
                PSP -> Config(RadialPadConfigs.PSP_LEFT, RadialPadConfigs.PSP_RIGHT)
                SNES -> Config(RadialPadConfigs.SNES_LEFT, RadialPadConfigs.SNES_RIGHT)
                GBA -> Config(RadialPadConfigs.GBA_LEFT, RadialPadConfigs.GBA_RIGHT)
                GENESIS_3 -> Config(RadialPadConfigs.GENESIS_3_LEFT, RadialPadConfigs.GENESIS_3_RIGHT)
                GENESIS_6 -> Config(RadialPadConfigs.GENESIS_6_LEFT, RadialPadConfigs.GENESIS_6_RIGHT, 1.0f, 1.2f)
                ATARI2600 -> Config(RadialPadConfigs.ATARI2600_LEFT, RadialPadConfigs.ATARI2600_RIGHT)
                SMS -> Config(RadialPadConfigs.SMS_LEFT, RadialPadConfigs.SMS_RIGHT)
                GG -> Config(RadialPadConfigs.GG_LEFT, RadialPadConfigs.GG_RIGHT)
                ARCADE_4 -> Config(RadialPadConfigs.ARCADE_4_LEFT, RadialPadConfigs.ARCADE_4_RIGHT)
                ARCADE_6 -> Config(RadialPadConfigs.ARCADE_6_LEFT, RadialPadConfigs.ARCADE_6_RIGHT, 1.0f, 1.2f)
                LYNX -> Config(RadialPadConfigs.LYNX_LEFT, RadialPadConfigs.LYNX_RIGHT)
                ATARI7800 -> Config(RadialPadConfigs.ATARI7800_LEFT, RadialPadConfigs.ATARI7800_RIGHT)
                PCE -> Config(RadialPadConfigs.PCE_LEFT, RadialPadConfigs.PCE_RIGHT)
            }
        }
    }
}
