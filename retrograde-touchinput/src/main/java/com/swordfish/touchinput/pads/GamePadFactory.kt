package com.swordfish.touchinput.pads

import android.content.Context

class GamePadFactory {
    enum class Layout {
        NES,
        SNES,
        GBA,
        GENESIS
    }

    companion object {
        fun getGamePadView(context: Context, layout: Layout): BaseGamePad {
            return when (layout) {
                Layout.NES -> GameBoyPad(context)
                Layout.SNES -> SNESPad(context)
                Layout.GENESIS -> GenesisPad(context)
                Layout.GBA -> GameBoyAdvancePad(context)
            }
        }
    }
}
