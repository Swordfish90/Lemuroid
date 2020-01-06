package com.swordfish.touchinput.pads

import android.content.Context

class GamePadFactory {
    enum class Layout {
        NES,
        SNES,
        GBA,
        GENESIS,
        N64,
        PSX,
        SMS
    }

    companion object {
        fun getGamePadView(context: Context, layout: Layout): BaseGamePad {
            return when (layout) {
                Layout.NES -> GameBoyPad(context)
                Layout.SNES -> SNESPad(context)
                Layout.GENESIS -> GenesisPad(context)
                Layout.GBA -> GameBoyAdvancePad(context)
                Layout.PSX -> PSXPad(context)
                Layout.N64 -> N64Pad(context)
                Layout.SMS -> SegaMasterSystemPad(context)
            }
        }
    }
}
