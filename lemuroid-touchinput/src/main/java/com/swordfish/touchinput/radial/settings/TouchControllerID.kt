package com.swordfish.touchinput.radial.settings

import com.swordfish.touchinput.radial.LemuroidTouchConfigs

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
    WS_PORTRAIT,
    NINTENDO_3DS,
    ;

    class Config(
        val leftConfig: LemuroidTouchConfigs.Kind,
        val rightConfig: LemuroidTouchConfigs.Kind,
        val leftScale: Float = 1.0f,
        val rightScale: Float = 1.0f,
        val verticalMarginDP: Float = 0f,
    )

    companion object {
        fun getConfig(id: TouchControllerID): Config {
            return when (id) {
                GB ->
                    Config(
                        LemuroidTouchConfigs.Kind.GB_LEFT,
                        LemuroidTouchConfigs.Kind.GB_RIGHT,
                    )
                NES ->
                    Config(
                        LemuroidTouchConfigs.Kind.NES_LEFT,
                        LemuroidTouchConfigs.Kind.NES_RIGHT,
                    )
                DESMUME ->
                    Config(
                        LemuroidTouchConfigs.Kind.DESMUME_LEFT,
                        LemuroidTouchConfigs.Kind.DESMUME_RIGHT,
                    )
                MELONDS ->
                    Config(
                        LemuroidTouchConfigs.Kind.MELONDS_NDS_LEFT,
                        LemuroidTouchConfigs.Kind.MELONDS_NDS_RIGHT,
                    )
                PSX ->
                    Config(
                        LemuroidTouchConfigs.Kind.PSX_LEFT,
                        LemuroidTouchConfigs.Kind.PSX_RIGHT,
                    )
                PSX_DUALSHOCK ->
                    Config(
                        LemuroidTouchConfigs.Kind.PSX_DUALSHOCK_LEFT,
                        LemuroidTouchConfigs.Kind.PSX_DUALSHOCK_RIGHT,
                    )
                N64 ->
                    Config(
                        LemuroidTouchConfigs.Kind.N64_LEFT,
                        LemuroidTouchConfigs.Kind.N64_RIGHT,
                        verticalMarginDP = 8f,
                    )
                PSP ->
                    Config(
                        LemuroidTouchConfigs.Kind.PSP_LEFT,
                        LemuroidTouchConfigs.Kind.PSP_RIGHT,
                    )
                SNES ->
                    Config(
                        LemuroidTouchConfigs.Kind.SNES_LEFT,
                        LemuroidTouchConfigs.Kind.SNES_RIGHT,
                    )
                GBA ->
                    Config(
                        LemuroidTouchConfigs.Kind.GBA_LEFT,
                        LemuroidTouchConfigs.Kind.GBA_RIGHT,
                    )
                GENESIS_3 ->
                    Config(
                        LemuroidTouchConfigs.Kind.GENESIS_3_LEFT,
                        LemuroidTouchConfigs.Kind.GENESIS_3_RIGHT,
                    )
                GENESIS_6 ->
                    Config(
                        LemuroidTouchConfigs.Kind.GENESIS_6_LEFT,
                        LemuroidTouchConfigs.Kind.GENESIS_6_RIGHT,
                        1.0f,
                        1.2f,
                    )
                ATARI2600 ->
                    Config(
                        LemuroidTouchConfigs.Kind.ATARI2600_LEFT,
                        LemuroidTouchConfigs.Kind.ATARI2600_RIGHT,
                    )
                SMS ->
                    Config(
                        LemuroidTouchConfigs.Kind.SMS_LEFT,
                        LemuroidTouchConfigs.Kind.SMS_RIGHT,
                    )
                GG ->
                    Config(
                        LemuroidTouchConfigs.Kind.GG_LEFT,
                        LemuroidTouchConfigs.Kind.GG_RIGHT,
                    )
                ARCADE_4 ->
                    Config(
                        LemuroidTouchConfigs.Kind.ARCADE_4_LEFT,
                        LemuroidTouchConfigs.Kind.ARCADE_4_RIGHT,
                    )
                ARCADE_6 ->
                    Config(
                        LemuroidTouchConfigs.Kind.ARCADE_6_LEFT,
                        LemuroidTouchConfigs.Kind.ARCADE_6_RIGHT,
                        1.0f,
                        1.2f,
                    )
                LYNX ->
                    Config(
                        LemuroidTouchConfigs.Kind.LYNX_LEFT,
                        LemuroidTouchConfigs.Kind.LYNX_RIGHT,
                    )
                ATARI7800 ->
                    Config(
                        LemuroidTouchConfigs.Kind.ATARI7800_LEFT,
                        LemuroidTouchConfigs.Kind.ATARI7800_RIGHT,
                    )
                PCE ->
                    Config(
                        LemuroidTouchConfigs.Kind.PCE_LEFT,
                        LemuroidTouchConfigs.Kind.PCE_RIGHT,
                    )
                NGP ->
                    Config(
                        LemuroidTouchConfigs.Kind.NGP_LEFT,
                        LemuroidTouchConfigs.Kind.NGP_RIGHT,
                    )
                DOS ->
                    Config(
                        LemuroidTouchConfigs.Kind.DOS_LEFT,
                        LemuroidTouchConfigs.Kind.DOS_RIGHT,
                    )
                WS_LANDSCAPE ->
                    Config(
                        LemuroidTouchConfigs.Kind.WS_LANDSCAPE_LEFT,
                        LemuroidTouchConfigs.Kind.WS_LANDSCAPE_RIGHT,
                    )
                WS_PORTRAIT ->
                    Config(
                        LemuroidTouchConfigs.Kind.WS_PORTRAIT_LEFT,
                        LemuroidTouchConfigs.Kind.WS_PORTRAIT_RIGHT,
                    )
                NINTENDO_3DS ->
                    Config(
                        LemuroidTouchConfigs.Kind.NINTENDO_3DS_LEFT,
                        LemuroidTouchConfigs.Kind.NINTENDO_3DS_RIGHT,
                    )
            }
        }
    }
}
