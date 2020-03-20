package com.swordfish.lemuroid.app.tv.game

import android.widget.Toast
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.libretrodroid.GLRetroView

class TVGameActivity : BaseGameActivity() {

    override fun displayToast(id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
    }

    override fun getDialogClass() = TVGameMenuActivity::class.java

    override fun getShaderForSystem(useShader: Boolean, system: GameSystem): Int {
        return GLRetroView.SHADER_CRT
    }
}
