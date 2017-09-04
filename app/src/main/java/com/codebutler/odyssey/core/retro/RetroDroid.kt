package com.codebutler.odyssey.core.retro

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Handler
import android.util.Log
import com.codebutler.odyssey.core.kotlin.toHexString
import com.sun.jna.Pointer
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class RetroDroid(private val context: Context) : Retro.Callback {

    private val handler = Handler()

    private var videoBytesPerPixel: Int = 0

    var videoCallback: ((bitmap: Bitmap) -> Unit)? = null

    fun start() {
        System.setProperty("jna.debug_load", "true")
        System.setProperty("jna.dump_memory", "true")
        System.setProperty("jna.library.path", context.cacheDir.absolutePath)

        val output = BufferedOutputStream(FileOutputStream(File(context.cacheDir.absoluteFile, "libsnes9x_libretro_android.so")))

        context.resources.assets.open("snes9x_libretro_android.so").copyTo(output)

        output.flush()
        output.close()

        val retro = Retro("snes9x_libretro_android")

        retro.logCallback = { level: LibRetro.retro_log_level, message: String ->
            when (level) {
                LibRetro.retro_log_level.RETRO_LOG_DEBUG -> Log.d("RetroLog", message)
                LibRetro.retro_log_level.RETRO_LOG_INFO -> Log.i("RetroLog", message)
                LibRetro.retro_log_level.RETRO_LOG_WARN -> Log.w("RetroLog", message)
                LibRetro.retro_log_level.RETRO_LOG_ERROR -> Log.e("RetroLog", message)
                else -> { }
            }
        }

        retro.videoCallback = videoCallback@ { data, width, height, pitch ->
            if (data == Pointer.NULL || data.getByte(0) == 0x0.toByte()) {
                return@videoCallback
            }

            val buffer = data.getByteArray(0, height * pitch)

            val newBuffer = ByteArray(width * height * videoBytesPerPixel)
            for (i in 0 until height) {
                System.arraycopy(
                        buffer,                             // SRC
                        i * pitch,                      // SRC POS
                        newBuffer,                          // DST
                        i * width * videoBytesPerPixel, // DST POS
                        width * videoBytesPerPixel      // LENGTH
                )
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(newBuffer))

            handler.post {
                videoCallback?.invoke(bitmap)
            }
        }

        retro.setEnvironment(this)

        retro.setVideoRefresh()

        retro.setAudioSample()

        retro.setAudioSampleBatch()

        retro.setInputPoll()

        retro.setInputState()

        retro.init()

        val gameLoaded = retro.loadGame()
        Log.d("Retro", "GAME LOADED!! $gameLoaded")

        Log.d("Retro", "Got Region: ${retro.getRegion()}")
        Log.d("Retro", "Got Info: ${retro.getSystemInfo()}")
        Log.d("Retro", "Got AV Info: ${retro.getSystemAVInfo()}")



        Thread {
            while (true) {
                retro.run()
                //Thread.sleep(10)
            }
        }.start()
    }

    override fun onSetVariables(variables: Map<String, String>) {
        Log.d("Retro", "onSetVariables: $variables")
    }

    override fun onSetSupportAchievements(supportsAchievements: Boolean) {
        Log.d("Retro", "onSetSupportAchievements: $supportsAchievements")
    }

    override fun onSetPerformanceLevel(performanceLevel: Int) {
        Log.d("Retro", "onSetPerformanceLevel: $performanceLevel")
    }

    override fun onGetVariable(name: String): String? {
        Log.d("Retro", "onGetVariable: $name")
        return null
    }

    override fun onSetPixelFormat(retroPixelFormat: LibRetro.retro_pixel_format): Boolean {
        val pixelFormat = when (retroPixelFormat) {
            LibRetro.retro_pixel_format .RETRO_PIXEL_FORMAT_0RGB1555 -> {
                // The image is stored using a 16-bit RGB format (5-5-5). The unused most significant bit is always zero.
                PixelFormat.RGBA_5551
            }
            LibRetro.retro_pixel_format.RETRO_PIXEL_FORMAT_XRGB8888 -> {
                PixelFormat.RGB_332 // FIXME Not sure if right. Should be 32-bit RGB format (0xffRRGGBB).
            }
            LibRetro.retro_pixel_format.RETRO_PIXEL_FORMAT_RGB565 -> {
                // The image is stored using a 16-bit RGB format (5-6-5).
                PixelFormat.RGB_565
            }
            LibRetro.retro_pixel_format.RETRO_PIXEL_FORMAT_UNKNOWN -> TODO()
        }

        val pixelFormatInfo = PixelFormat()
        PixelFormat.getPixelFormatInfo(pixelFormat, pixelFormatInfo)
        Log.d("Retro", "onSetPixelFormat: $pixelFormat " +
                "bitsPerPixel: ${pixelFormatInfo.bitsPerPixel} " +
                "bytesPerPixel: ${pixelFormatInfo.bytesPerPixel}")

        videoBytesPerPixel = pixelFormatInfo.bytesPerPixel

        return true
    }

    override fun onSetInputDescriptors(descriptors: List<Retro.InputDescriptor>) {
        Log.d("Retro", "onSetInputDescriptors: $descriptors")
    }

    override fun onSetControllerInfo(info: List<Retro.ControllerInfo>) {
        Log.d("Retro", "onSetControllerInfo: $info")
    }

    override fun onGetVariableUpdate(): Boolean {
        Log.d("Retro", "onGetVariableUpdate")
        return false
    }

    override fun onSetMemoryMaps() {
        Log.d("Retro", "onSetMemoryMaps")
    }
}
