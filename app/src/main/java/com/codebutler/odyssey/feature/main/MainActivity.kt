package com.codebutler.odyssey.feature.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.codebutler.odyssey.feature.game.GameActivity
import com.codebutler.odyssey.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button_launch)
        button.setOnClickListener {
            startActivity(GameActivity.newIntent(
                    context = this,
                    coreFileName = "snes9x_libretro_android.so",
                    gameFileName = "Super Mario All-Stars (U) [!].smc"))
        }
    }
}
