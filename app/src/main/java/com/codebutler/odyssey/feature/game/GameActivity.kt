/*
 * GameActivity.kt
 *
 * Copyright (C) 2017 Odyssey Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.codebutler.odyssey.feature.game

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.ImageView
import com.codebutler.odyssey.R
import com.codebutler.odyssey.core.kotlin.bindView
import com.codebutler.odyssey.core.retro.RetroDroid
import com.codebutler.odyssey.core.retro.Retro
import java.io.File

class GameActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_FILE_CORE = "file_core"
        private const val EXTRA_FILE_GAME = "file_game"

        fun newIntent(context: Context, coreFilePath: String, gameFilePath: String): Intent {
            return Intent(context, GameActivity::class.java).apply {
                putExtra(EXTRA_FILE_CORE, coreFilePath)
                putExtra(EXTRA_FILE_GAME, gameFilePath)
            }
        }
    }

    private val imageView: ImageView by bindView(R.id.image)

    private val handler = Handler()

    private var retroDroid: RetroDroid? = null
    private var audioTrack: AudioTrack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        loadRetro()
    }

    override fun onResume() {
        super.onResume()
        retroDroid?.start()
    }

    override fun onPause() {
        super.onPause()
        retroDroid?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        retroDroid?.unloadGame()
        retroDroid?.deinit()

        // This activity runs in its own process which should not live beyond the activity lifecycle.
        System.exit(0)
    }

    private fun loadRetro() {
        val coreFilePath = intent.getStringExtra(EXTRA_FILE_CORE)
        val gameFilePath = intent.getStringExtra(EXTRA_FILE_GAME)

        val retroDroid = RetroDroid(this, File(coreFilePath))

        retroDroid.logCallback = { level, message ->
            val tag = "RetroLog"
            when (level) {
                Retro.LogLevel.DEBUG -> Log.d(tag, message)
                Retro.LogLevel.INFO -> Log.i(tag, message)
                Retro.LogLevel.WARN -> Log.w(tag, message)
                Retro.LogLevel.ERROR -> Log.e(tag, message)
            }
        }

        retroDroid.prepareAudioCallback = { sampleRate ->
            audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build())
                    .build()
        }

        retroDroid.videoCallback = { bitmap ->
            handler.post {
                imageView.setImageBitmap(bitmap)
            }
        }

        retroDroid.audioCallback = { buffer ->
             audioTrack?.let { audioTrack ->
                 audioTrack.write(buffer, 0, buffer.size)
                 audioTrack.play()
             }
        }

        retroDroid.loadGame(gameFilePath)
        retroDroid.start()

        this.retroDroid = retroDroid
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        super.dispatchGenericMotionEvent(event)
        retroDroid?.onMotionEvent(event)
        return true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        super.dispatchKeyEvent(event)
        Log.d("Retro", "dispatchKeyEvent ${event.action} ${event.keyCode}")
        retroDroid?.onKeyEvent(event)
        return true
    }
}
