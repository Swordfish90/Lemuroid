package com.codebutler.odyssey

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.codebutler.odyssey.core.retro.RetroDroid
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Structure

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSION = 10001

    lateinit private var imageView: ImageView

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
        val retroDroid = RetroDroid(this)
        retroDroid.videoCallback = { bitmap ->
            imageView.setImageBitmap(bitmap)
        }
        retroDroid.start()
    }
}
