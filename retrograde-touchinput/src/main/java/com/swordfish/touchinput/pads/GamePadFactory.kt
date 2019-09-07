package com.swordfish.touchinput.pads

import android.content.Context

class GamePadFactory {
    enum class Layout {
        NES,
        SNES
    }

    companion object {
        fun getGamePadView(context: Context, layout: Layout): GamePadView {
            return when (layout) {
                Layout.NES -> GameBoyPad(context)
                Layout.SNES -> GameBoyAdvancePad(context)
            }
        }
    }
}
