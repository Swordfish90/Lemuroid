package com.swordfish.touchinput.pads

import android.content.Context
import com.swordfish.lemuroid.lib.library.GameSystem

class GamePadFactory {
    companion object {
        fun getGamePadView(context: Context, systemId: String): BaseGamePad {
            return when (systemId) {
                in listOf(GameSystem.GBA_ID) -> GameBoyAdvancePad(context)
                in listOf(GameSystem.SNES_ID) -> SNESPad(context)
                in listOf(GameSystem.NES_ID, GameSystem.GB_ID, GameSystem.GBC_ID) -> GameBoyPad(context)
                in listOf(GameSystem.GENESIS_ID) -> GenesisPad(context)
                in listOf(GameSystem.N64_ID) -> N64Pad(context)
                in listOf(GameSystem.SMS_ID) -> SegaMasterSystemPad(context)
                in listOf(GameSystem.PSP_ID) -> PSPPad(context)
                in listOf(GameSystem.ARCADE_FB_NEO) -> ArcadePad(context)
                else -> PSXPad(context)
            }
        }
    }
}
