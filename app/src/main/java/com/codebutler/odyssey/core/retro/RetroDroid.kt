package com.codebutler.odyssey.core.retro

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.util.Log
import com.codebutler.odyssey.core.retro.lib.LibRetro
import com.codebutler.odyssey.core.retro.lib.Retro
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask

/**
 * Native Android frontend for LibRetro!
 */
class RetroDroid(private val context: Context, private val coreFileName: String) :
        Retro.EnvironmentCallback,
        Retro.VideoRefreshCallback,
        Retro.AudioSampleCallback,
        Retro.AudioSampleBatchCallback,
        Retro.InputPollCallback,
        Retro.InputStateCallback {

    private val retro: Retro
    private val timer = Timer()

    private var videoPixelFormat: Int = PixelFormat.RGBA_8888
    private var videoBitmapConfig: Bitmap.Config = Bitmap.Config.ARGB_8888
    private var videoBytesPerPixel: Int = 0

    var logCallback: ((level: Retro.LogLevel, message: String) -> Unit)? = null

    var videoCallback: ((bitmap: Bitmap) -> Unit)? = null

    var audioCallback: ((buffer: ByteArray) -> Unit)? = null

    init {
        System.setProperty("jna.debug_load", "true")
        System.setProperty("jna.dump_memory", "true")
        System.setProperty("jna.library.path", context.cacheDir.absolutePath)

        val coreName = copyCoreToCacheDir()

        retro = Retro(coreName)

        retro.logCallback = { level: LibRetro.retro_log_level, message: String ->
            when (level) {
                LibRetro.retro_log_level.RETRO_LOG_DEBUG -> logCallback?.invoke(Retro.LogLevel.DEBUG, message)
                LibRetro.retro_log_level.RETRO_LOG_INFO -> logCallback?.invoke(Retro.LogLevel.INFO, message)
                LibRetro.retro_log_level.RETRO_LOG_WARN -> logCallback?.invoke(Retro.LogLevel.WARN, message)
                LibRetro.retro_log_level.RETRO_LOG_ERROR -> logCallback?.invoke(Retro.LogLevel.ERROR, message)
                else -> { }
            }
        }

        retro.setEnvironment(this)
        retro.setVideoRefresh(this)
        retro.setAudioSample(this)
        retro.setAudioSampleBatch(this)
        retro.setInputPoll(this)
        retro.setInputState(this)
        retro.init()
    }

    fun loadGame(filePath: String) {
        if (!retro.loadGame(filePath)) {
            throw Exception("Failed to load game")
        }

        Log.d("Retro", "GAME LOADED!!")
        Log.d("Retro", "Got Region: ${retro.getRegion()}")
        Log.d("Retro", "Got Info: ${retro.getSystemInfo()}")
        Log.d("Retro", "Got AV Info: ${retro.getSystemAVInfo()}")
    }

    fun start() {
        // FIXME: Implement proper timing
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                retro.run()
            }
        }, 0, 10)
    }

    private fun copyCoreToCacheDir(): String {
        val cachedCoreFile = File(context.cacheDir.absoluteFile, "lib$coreFileName")
        val stream = BufferedOutputStream(FileOutputStream(cachedCoreFile))
        stream.use { output ->
            context.resources.assets.open(coreFileName).copyTo(output)
            output.flush()
        }
        return coreFileName.substringBeforeLast(".")
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

        // FIXME: This will likely need to be replaced with a conversion function
        val bitmapConfig = when (pixelFormat) {
            PixelFormat.RGB_565 -> Bitmap.Config.RGB_565
            else -> TODO()
        }

        val pixelFormatInfo = PixelFormat()
        PixelFormat.getPixelFormatInfo(pixelFormat, pixelFormatInfo)
        Log.d("Retro", "onSetPixelFormat: $pixelFormat " +
                "bitsPerPixel: ${pixelFormatInfo.bitsPerPixel} " +
                "bytesPerPixel: ${pixelFormatInfo.bytesPerPixel}")

        videoPixelFormat = pixelFormat
        videoBitmapConfig = bitmapConfig
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

    override fun onVideoRefresh(data: ByteArray?, width: Int, height: Int, pitch: Int) {
        data ?: return

        val newBuffer = ByteArray(width * height * videoBytesPerPixel)
        for (i in 0 until height) {
            System.arraycopy(
                    data,                           // SRC
                    i * pitch,                      // SRC POS
                    newBuffer,                      // DST
                    i * width * videoBytesPerPixel, // DST POS
                    width * videoBytesPerPixel      // LENGTH
            )
        }

        val bitmap = Bitmap.createBitmap(width, height, videoBitmapConfig)
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(newBuffer))

        videoCallback?.invoke(bitmap)
    }

    override fun onAudioSample(left: Short, right: Short) {
        // FIXME: Implement audio correctly
        val buffer = ByteArray(2)
        //buffer[0] = left.toByte()
        //buffer[1] = right.toByte()
        //audioCallback?.invoke(buffer)
    }

    override fun onAudioSampleBatch(data: ByteArray, frames: Int): Long {
        // FIXME: Implement audio correctly
        //audioCallback?.invoke(data)
        return 0
    }

    override fun onInputPoll() {
        //Log.d("RETRO", "ON INPUT POLL")
    }

    override fun onInputState(port: Int, device: Int, index: Int, id: Int): Short {
        //Log.d("RETRO", "ON INPUT STATE!! $port $device $index $id")
        return 0
    }
}
