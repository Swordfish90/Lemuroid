package com.swordfish.touchinput.pads

import android.content.Context
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemID

class GamePadFactory {
    companion object {
        fun getGamePadView(context: Context, system: GameSystem): BaseGamePad {
            return when (system.id) {
                SystemID.GBA -> GameBoyAdvancePad(context)
                SystemID.NES -> GameBoyPad(context)
                SystemID.GB -> GameBoyPad(context)
                SystemID.GBC -> GameBoyPad(context)
                SystemID.SNES -> SNESPad(context)
                SystemID.GENESIS -> GenesisPad(context)
                SystemID.N64 -> N64Pad(context)
                SystemID.SMS -> SegaMasterSystemPad(context)
                SystemID.PSP -> PSPPad(context)
                SystemID.FBNEO -> ArcadePad(context)
            }
        }
    }
}
