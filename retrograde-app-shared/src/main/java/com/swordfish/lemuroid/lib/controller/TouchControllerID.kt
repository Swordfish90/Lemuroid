package com.swordfish.lemuroid.lib.controller

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
    PCE,
    NGP,
    DOS,
    WS_LANDSCAPE,
    WS_PORTRAIT;

    class Config(
        val leftConfig: RadialPadConfigs.Kind,
        val rightConfig: RadialPadConfigs.Kind,
        val leftScale: Float = 1.0f,
        val rightScale: Float = 1.0f
    )

    companion object {
        fun getConfig(id: TouchControllerID): Config {
            return when (id) {
                GB -> Config(RadialPadConfigs.Kind.GB_LEFT, RadialPadConfigs.Kind.GB_RIGHT)
                NES -> Config(RadialPadConfigs.Kind.NES_LEFT, RadialPadConfigs.Kind.NES_RIGHT)
                DESMUME -> Config(RadialPadConfigs.Kind.DESMUME_LEFT, RadialPadConfigs.Kind.DESMUME_RIGHT)
                MELONDS -> Config(RadialPadConfigs.Kind.MELONDS_NDS_LEFT, RadialPadConfigs.Kind.MELONDS_NDS_RIGHT)
                PSX -> Config(RadialPadConfigs.Kind.PSX_LEFT, RadialPadConfigs.Kind.PSX_RIGHT)
                PSX_DUALSHOCK -> Config(RadialPadConfigs.Kind.PSX_DUALSHOCK_LEFT, RadialPadConfigs.Kind.PSX_DUALSHOCK_RIGHT)
                N64 -> Config(RadialPadConfigs.Kind.N64_LEFT, RadialPadConfigs.Kind.N64_RIGHT)
                PSP -> Config(RadialPadConfigs.Kind.PSP_LEFT, RadialPadConfigs.Kind.PSP_RIGHT)
                SNES -> Config(RadialPadConfigs.Kind.SNES_LEFT, RadialPadConfigs.Kind.SNES_RIGHT)
                GBA -> Config(RadialPadConfigs.Kind.GBA_LEFT, RadialPadConfigs.Kind.GBA_RIGHT)
                GENESIS_3 -> Config(RadialPadConfigs.Kind.GENESIS_3_LEFT, RadialPadConfigs.Kind.GENESIS_3_RIGHT)
                GENESIS_6 -> Config(RadialPadConfigs.Kind.GENESIS_6_LEFT, RadialPadConfigs.Kind.GENESIS_6_RIGHT, 1.0f, 1.2f)
                ATARI2600 -> Config(RadialPadConfigs.Kind.ATARI2600_LEFT, RadialPadConfigs.Kind.ATARI2600_RIGHT)
                SMS -> Config(RadialPadConfigs.Kind.SMS_LEFT, RadialPadConfigs.Kind.SMS_RIGHT)
                GG -> Config(RadialPadConfigs.Kind.GG_LEFT, RadialPadConfigs.Kind.GG_RIGHT)
                ARCADE_4 -> Config(RadialPadConfigs.Kind.ARCADE_4_LEFT, RadialPadConfigs.Kind.ARCADE_4_RIGHT)
                ARCADE_6 -> Config(RadialPadConfigs.Kind.ARCADE_6_LEFT, RadialPadConfigs.Kind.ARCADE_6_RIGHT, 1.0f, 1.2f)
                LYNX -> Config(RadialPadConfigs.Kind.LYNX_LEFT, RadialPadConfigs.Kind.LYNX_RIGHT)
                ATARI7800 -> Config(RadialPadConfigs.Kind.ATARI7800_LEFT, RadialPadConfigs.Kind.ATARI7800_RIGHT)
                PCE -> Config(RadialPadConfigs.Kind.PCE_LEFT, RadialPadConfigs.Kind.PCE_RIGHT)
                NGP -> Config(RadialPadConfigs.Kind.NGP_LEFT, RadialPadConfigs.Kind.NGP_RIGHT)
                DOS -> Config(RadialPadConfigs.Kind.DOS_LEFT, RadialPadConfigs.Kind.DOS_RIGHT)
                WS_LANDSCAPE -> Config(RadialPadConfigs.Kind.WS_LANDSCAPE_LEFT, RadialPadConfigs.Kind.WS_LANDSCAPE_RIGHT)
                WS_PORTRAIT -> Config(RadialPadConfigs.Kind.WS_PORTRAIT_LEFT, RadialPadConfigs.Kind.WS_PORTRAIT_RIGHT)
            }
        }
    }
}
