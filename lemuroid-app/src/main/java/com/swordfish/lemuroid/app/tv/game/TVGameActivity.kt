package com.swordfish.lemuroid.app.tv.game

import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.tv.gamemenu.TVGameMenuActivity
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.libretrodroid.GLRetroView

class TVGameActivity : BaseGameActivity() {

    override fun getDialogClass() = TVGameMenuActivity::class.java

    override fun getShaderForSystem(useShader: Boolean, system: GameSystem): Int {
        return if (useShader) GLRetroView.SHADER_CRT else GLRetroView.SHADER_DEFAULT
    }
}
