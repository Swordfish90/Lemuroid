/*
 * RetroDroid.kt
 *
 * Copyright (C) 2017 Retrograde Project
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

package com.codebutler.retrograde.lib.retro

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.codebutler.retrograde.common.BitmapCache
import com.codebutler.retrograde.common.BufferCache
import com.codebutler.retrograde.common.BuildConfig
import com.codebutler.retrograde.lib.binding.LibRetrograde
import com.codebutler.retrograde.lib.game.audio.GameAudio
import com.codebutler.retrograde.lib.game.display.FpsCalculator
import com.codebutler.retrograde.lib.game.display.GameDisplay
import com.codebutler.retrograde.lib.game.input.GameInput
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.jakewharton.rxrelay2.PublishRelay
import com.sun.jna.Native
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import kotlin.experimental.and

/**
 * Native Android frontend for LibRetro!
 */
class RetroDroid(
    private val gameDisplay: GameDisplay,
    private val gameAudio: GameAudio,
    private val gameInput: GameInput,
    private val context: Context,
    coreFile: File
) : DefaultLifecycleObserver {
    private val audioSampleBufferCache = BufferCache()
    private val fpsCalculator = FpsCalculator()
    private val handler = Handler()
    private val retro: Retro
    private val variables: MutableMap<String, Retro.Variable> = mutableMapOf()
    private val videoBufferCache = BufferCache()
    private val videoBitmapCache = BitmapCache()

    private val gameUnloadedRelay = PublishRelay.create<Optional<ByteArray>>()

    private var region: Retro.Region? = null
    private var systemAVInfo: Retro.SystemAVInfo? = null
    private var systemInfo: Retro.SystemInfo? = null
    private var thread: RetroThread? = null
    private var pixelFormat: Retro.PixelFormat? = null

    val fps: Long
        get() = fpsCalculator.fps

    /**
     * Callback when game is unloaded, to allow for persisting save ram.
     */
    val gameUnloaded: Observable<Optional<ByteArray>> = gameUnloadedRelay.hide()

    init {
        Native.setCallbackExceptionHandler { c, e ->
            handler.post {
                throw Exception("JNA: Callback $c threw exception", e)
            }
        }

        System.setProperty("jna.library.path", coreFile.parentFile.absolutePath)

        if (BuildConfig.DEBUG) {
            val stdoutFile = File(context.filesDir, "stdout.log")
            val stderrFile = File(context.filesDir, "stderr.log")
            LibRetrograde.INSTANCE.retrograde_redirect_stdio(stdoutFile.path, stderrFile.path)
        }

        val coreLibraryName = coreFile.nameWithoutExtension.substring(3) // FIXME

        retro = Retro(coreLibraryName)

        retro.environmentCallback = RetroDroidEnvironmentCallback()

        retro.videoCallback = { data, width, height, pitch ->
            val pixelFormat = pixelFormat!!
            val videoBytesPerPixel = pixelFormat.bytesPerPixel

            val newBuffer = videoBufferCache.getBuffer(width * height * videoBytesPerPixel)
            for (i in 0 until height) {
                val widthAsBytes = width * videoBytesPerPixel
                System.arraycopy(
                    data, // SRC
                    i * pitch, // SRC POS
                    newBuffer, // DST
                    i * widthAsBytes, // DST POS
                    widthAsBytes // LENGTH
                )
            }

            // This is actually BGRx
            if (pixelFormat == Retro.PixelFormat.XRGB8888) {
                for (i in 0 until newBuffer.size step videoBytesPerPixel) {
                    val r = newBuffer[i + 2]
                    val b = newBuffer[i]
                    newBuffer[i] = r
                    newBuffer[i + 2] = b
                    newBuffer[i + 3] = 0xFF.toByte()
                }
            }

            val bitmap = videoBitmapCache.getBitmap(width, height, pixelFormat.bitmapConfig)
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(newBuffer))
            gameDisplay.update(bitmap)
        }

        retro.audioSampleCallback = { left, right ->
            val buffer = audioSampleBufferCache.getBuffer(2)
            buffer[0] = left.toByte() and 0xff.toByte()
            buffer[1] = (right.toInt() shr 8).toByte() and 0xff.toByte()
            gameAudio.play(buffer)
        }

        retro.audioSampleBatchCallback = { data ->
            gameAudio.play(data).toLong()
        }

        retro.inputPollCallback = { /* Nothing to do here? */ }

        retro.inputStateCallback = this::onInputState

        retro.init()
    }

    fun loadGame(gamePath: String, saveData: ByteArray?) {
        val systemInfo = retro.getSystemInfo()
        Timber.d("System Info: $systemInfo")

        if (systemInfo.needFullpath) {
            if (!retro.loadGame(gamePath)) {
                throw Exception("Failed to load game via path: $gamePath")
            }
        } else {
            Timber.d("Load game with data!!")
            if (!retro.loadGame(File(gamePath).readBytes())) {
                throw Exception("Failed to load game via buffer: $gamePath")
            }
        }

        Timber.d("Game loaded!")

        val region = retro.getRegion()
        Timber.d("Region: $region")

        val systemAVInfo = retro.getSystemAVInfo()
        Timber.d("System AV Info: $systemAVInfo")
        updateSystemAVInfo(systemAVInfo)

        this.region = region
        this.systemInfo = systemInfo

        if (saveData != null) {
            retro.unserialize(saveData)
        }
    }

    fun start() {
        val avInfo = systemAVInfo
        if (this.thread != null || avInfo == null) {
            return
        }
        this.thread = RetroThread.fromFPS(avInfo.timing.fps) {
            retro.run()
            fpsCalculator.update()
        }
        this.thread?.priority = Thread.MAX_PRIORITY
        this.thread?.start()
    }

    fun stop() {
        thread?.interrupt()
        thread = null
    }

    @SuppressLint("CheckResult")
    fun unloadGame() {

        // There is a native crash if serialize is called immidiately after stop.
        Single.timer(160, TimeUnit.MILLISECONDS)
                .map { retro.serialize().toOptional() }
                .subscribeOn(Schedulers.io())
                .doOnSuccess { retro.unloadGame() }
                .subscribe(gameUnloadedRelay)
    }

    override fun onResume(owner: LifecycleOwner) {
        start()
    }

    override fun onPause(owner: LifecycleOwner) {
        stop()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        deinit()
    }

    private fun deinit() {
        gameInput.deinit()
        retro.deinit()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onInputState(port: Int, device: Int, index: Int, id: Int): Boolean =
            gameInput.isButtonPressed(port, device, id)

    private fun updateSystemAVInfo(systemAVInfo: Retro.SystemAVInfo) {
        gameAudio.init(systemAVInfo.timing.sample_rate.toInt())
        this.systemAVInfo = systemAVInfo
    }

    inner class RetroDroidEnvironmentCallback : Retro.EnvironmentCallback {
        override fun onGetLogInterface(): Retro.LogInterface? {
            // Retro logging is somewhat expensive, so skip entirely in production builds.
            if (Timber.treeCount() > 0) {
                return object : Retro.LogInterface {
                    override fun onLogMessage(level: Retro.LogLevel, message: String) {
                        val timber = Timber.tag("RetroLog")
                        when (level) {
                            Retro.LogLevel.DEBUG -> timber.d(message)
                            Retro.LogLevel.INFO -> timber.i(message)
                            Retro.LogLevel.WARN -> timber.w(message)
                            Retro.LogLevel.ERROR -> timber.e(message)
                        }
                    }
                }
            }
            return null
        }

        override fun onSetVariables(variables: Map<String, Retro.Variable>) {
            Timber.d("onSetVariables: $variables")
            this@RetroDroid.variables.putAll(variables)
        }

        override fun onSetSupportAchievements(supportsAchievements: Boolean) {
            // FIXME: Implement
            Timber.d("onSetSupportAchievements: $supportsAchievements")
        }

        override fun onSetPerformanceLevel(performanceLevel: Int) {
            // FIXME: Implement
            Timber.d("onSetPerformanceLevel: $performanceLevel")
        }

        override fun onSetSystemAvInfo(info: Retro.SystemAVInfo) {
            Timber.d("onSetSystemAvInfo: $info")
            updateSystemAVInfo(info)
        }

        override fun onSetGeometry(geometry: Retro.GameGeometry) {
            Timber.d("onSetGeometry: $geometry")
            val systemAVInfo = systemAVInfo ?: retro.getSystemAVInfo()
            updateSystemAVInfo(systemAVInfo.copy(geometry = geometry))
        }

        override fun onGetVariable(name: String): String? {
            Timber.d("onGetVariable: $name, value: ${variables[name]}")
            return variables[name]?.value
        }

        override fun onSetPixelFormat(pixelFormat: Retro.PixelFormat): Boolean {
            Timber.d("""onSetPixelFormat: $pixelFormat (bytesPerPixel: ${pixelFormat.bytesPerPixel})""")
            this@RetroDroid.pixelFormat = pixelFormat
            return true
        }

        override fun onSetInputDescriptors(descriptors: List<Retro.InputDescriptor>) {
            // FIXME: Implement
            Timber.d("onSetInputDescriptors: $descriptors")
        }

        override fun onSetControllerInfo(info: List<Retro.ControllerInfo>) {
            Timber.d("onSetControllerInfo: $info")
        }

        override fun onGetVariableUpdate(): Boolean {
            // FIXME: Implement
            // Timber.d("onGetVariableUpdate")
            return false
        }

        override fun onGetSystemDirectory(): String? {
            val dir = File(context.filesDir, "system")
            dir.mkdirs()
            Timber.d("onGetSystemDirectory ${dir.absolutePath}")
            return dir.absolutePath
        }

        override fun onGetSaveDirectory(): String? {
            val dir = File(context.filesDir, "save")
            dir.mkdirs()
            Timber.d("onGetSaveDirectory ${dir.absolutePath}")
            return dir.absolutePath
        }

        override fun onSetMemoryMaps() {
            // FIXME: Implement
            // Timber.d("onSetMemoryMaps")
        }

        override fun onUnsupportedCommand(cmd: Int) {
            Timber.e("Unsupported env command: $cmd")
        }

        override fun onUnhandledException(error: Throwable) {
            throw RuntimeException("Unhandled Exception in Environment Callback", error)
        }
    }
}
