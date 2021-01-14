package com.swordfish.lemuroid.app.shared.gamecrash

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.swordfish.lemuroid.R

class GameCrashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)

        val message = intent.getStringExtra(EXTRA_MESSAGE)?.let {
            getString(R.string.lemuroid_crash_message, it)
        }

        findViewById<TextView>(R.id.crashmessage).text = message
    }

    companion object {
        const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
    }
}
