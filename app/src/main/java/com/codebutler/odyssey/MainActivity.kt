package com.codebutler.odyssey

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.ImageView
import com.codebutler.odyssey.core.retro.RetroDroid
import com.codebutler.odyssey.core.retro.lib.Retro
import java.io.File

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSION = 10001

    lateinit private var imageView: ImageView

    private val handler = Handler()

    private var retroDroid: RetroDroid? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.image)

        val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
        } else {
            loadRetro()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.get(0) == PackageManager.PERMISSION_GRANTED) {
            loadRetro()
        }
    }

    private fun loadRetro() {
        val retroDroid = RetroDroid(this, "snes9x_libretro_android.so")

        retroDroid.logCallback = { level, message ->
            val tag = "RetroLog"
            when (level) {
                Retro.LogLevel.DEBUG -> Log.d(tag, message)
                Retro.LogLevel.INFO -> Log.i(tag, message)
                Retro.LogLevel.WARN -> Log.w(tag, message)
                Retro.LogLevel.ERROR -> Log.e(tag, message)
            }
        }

        retroDroid.videoCallback = { bitmap ->
            handler.post {
                imageView.setImageBitmap(bitmap)
            }
        }

        val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build())
                .setBufferSizeInBytes(4)
                .build()

        retroDroid.audioCallback = { buffer ->
             audioTrack.write(buffer, 0, buffer.size)
             audioTrack.play()
        }

        val gameFile = File(Environment.getExternalStorageDirectory(), "Super Mario All-Stars (U) [!].smc")
        retroDroid.loadGame(gameFile.absolutePath)
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
