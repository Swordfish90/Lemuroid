package com.codebutler.odyssey.core.retro.lib

import android.os.Build
import com.codebutler.odyssey.SizeT
import com.codebutler.odyssey.UnsignedInt
import com.codebutler.odyssey.core.retro.lib.LibRetro.retro_system_info
import com.codebutler.odyssey.core.retro.lib.LibRetro.retro_variable
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure

/**
 * Java idomatic wrapper around [LibRetro].
 */
class Retro(coreLibraryName: String) {

    private val libRetro : LibRetro = Native.loadLibrary(
            coreLibraryName,
            LibRetro::class.java,
            mapOf(Library.OPTION_STRUCTURE_ALIGNMENT to
                // Using ALIGN_DEFAULT on x86 caused issues with float field alignment (retro_system_timing).
                if ("x86" in Build.SUPPORTED_32_BIT_ABIS && Build.SUPPORTED_64_BIT_ABIS.isEmpty()) {
                    Structure.ALIGN_NONE
                } else {
                    Structure.ALIGN_DEFAULT
                }
            ))

    // These are here to prevent the GC from reaping our callbacks.
    private var videoRefreshCb: LibRetro.retro_video_refresh_t? = null
    private var envCb: LibRetro.retro_environment_t? = null
    private var audioSampleCb: LibRetro.retro_audio_sample_t? = null
    private var audioSampleBatchCb: LibRetro.retro_audio_sample_batch_t? = null
    private var inputPollCb: LibRetro.retro_input_poll_t? = null
    private var inputStateCb: LibRetro.retro_input_state_t? = null
    private var logPrintfCb: LibRetro.retro_log_printf_t? = null

    enum class LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    data class ControllerDescription(
            val desc: String,
            val id: Device)

    data class ControllerInfo(
            val types: List<ControllerDescription>)

    data class SystemInfo(
            val libraryName: String,
            val libraryVersion: String,
            val validExtensions: String,
            val needFullpath: Boolean,
            val blockExtract: Boolean)

    data class GameGeometry(
            val baseWidth: Int,
            val baseHeight: Int,
            val maxWidth: Int,
            var maxHeight: Int,
            var aspectRatio: Float)

    data class SystemTiming(
            val fps: Double,
            val sample_rate: Double)

    data class SystemAVInfo(
            val geometry: GameGeometry,
            val timing: SystemTiming)

    data class InputDescriptor(
            val port: Int,
            val device: Device,
            val index: Int,
            val id: DeviceId,
            val description: String)

    enum class Device(val value: Int) {
        NONE(0),
        JOYPAD(1),
        MOUSE(2),
        KEYBOARD(3),
        LIGHTGUN(4),
        ANALOG(5),
        POINTER(6),

        // FIXME: What's the deal with these...
        JOYPAD_MULTITAP ((1 shl 8) or JOYPAD.value),
        LIGHTGUN_SUPER_SCOPE ((1 shl 8) or LIGHTGUN.value),
        LIGHTGUN_JUSTIFIER ((2 shl 8) or LIGHTGUN.value),
        LIGHTGUN_JUSTIFIERS ((3 shl 8) or LIGHTGUN.value);

        companion object {
            private val valueCache = mapOf(*Device.values().map { it.value to it }.toTypedArray())
            fun fromValue(value: Int) = valueCache[value]!!
        }
    }

    enum class DeviceId(val value: Int) {
        JOYPAD_B(0),
        JOYPAD_Y(1),
        JOYPAD_SELECT(2),
        JOYPAD_START(3),
        JOYPAD_UP(4),
        JOYPAD_DOWN(5),
        JOYPAD_LEFT(6),
        JOYPAD_RIGHT(7),
        JOYPAD_A(8),
        JOYPAD_X(9),
        JOYPAD_L(10),
        JOYPAD_R(11),
        JOYPAD_L2(12),
        JOYPAD_R2(13),
        JOYPAD_L3(14),
        JOYPAD_R3(15);

        companion object {
            private val valueCache = mapOf(*DeviceId.values().map { it.value to it }.toTypedArray())
            fun fromValue(value: Int) = valueCache[value]!!
        }
    }

    enum class Region(val value: Int) {
        REGION_NTSC(0),
        REGION_PAL(1);

        companion object {
            private val valueCache = mapOf(*Region.values().map { it.value to it }.toTypedArray())
            fun fromValue(value: Int) = valueCache[value]!!
        }

    }

    interface EnvironmentCallback {

        fun onSetVariables(variables: Map<String, String>)

        fun onSetSupportAchievements(supportsAchievements: Boolean)

        fun onSetPerformanceLevel(performanceLevel: Int)

        fun onGetVariable(name: String): String?

        fun onSetPixelFormat(retroPixelFormat: Int): Boolean

        fun onSetInputDescriptors(descriptors: List<InputDescriptor>)

        fun onSetControllerInfo(info: List<ControllerInfo>)

        fun onGetVariableUpdate(): Boolean

        fun onSetMemoryMaps()

        fun onGetLogInterface(): LogInterface
    }

    interface LogInterface {

        fun onLogMessage(level: LogLevel, message: String)
    }

    interface VideoRefreshCallback {

        fun onVideoRefresh(data: ByteArray, width: Int, height: Int, pitch: Int)
    }

    interface AudioSampleCallback {

        fun onAudioSample(left: Short, right: Short)
    }

    interface AudioSampleBatchCallback {

        fun onAudioSampleBatch(data: ByteArray, frames: Int): Long
    }

    interface InputPollCallback {
        fun onInputPoll()
    }

    interface InputStateCallback {
        fun onInputState(port: Int, device: Int, index: Int, id: Int): Boolean
    }

    fun getSystemInfo(): SystemInfo {
        val info = retro_system_info()
        libRetro.retro_get_system_info(info)
        return SystemInfo(
                libraryName = info.library_name!!,
                libraryVersion = info.library_version!!,
                validExtensions = info.valid_extensions!!,
                needFullpath = info.need_fullpath,
                blockExtract = info.block_extract
        )
    }

    fun getSystemAVInfo(): SystemAVInfo {
        val info = LibRetro.retro_system_av_info()
        libRetro.retro_get_system_av_info(info)
        return SystemAVInfo(
                GameGeometry(
                        info.geometry!!.base_width!!.toInt(),
                        info.geometry!!.base_height!!.toInt(),
                        info.geometry!!.max_width!!.toInt(),
                        info.geometry!!.max_height!!.toInt(),
                        info.geometry!!.aspect_ratio!!
                ),
                SystemTiming(
                        info.timing!!.fps!!,
                        info.timing!!.sample_rate!!
                )
        )
    }

    fun getRegion(): Region {
        val region = libRetro.retro_get_region()
        return Region.fromValue(region.toInt())
    }

    fun setVideoRefresh(callback: VideoRefreshCallback) {

        val cb = object : LibRetro.retro_video_refresh_t {
            override fun invoke(data: Pointer, width: UnsignedInt, height: UnsignedInt, pitch: SizeT) {
                val buffer = data.getByteArray(0, height.toInt() * pitch.toInt())
                callback.onVideoRefresh(buffer, width.toInt(), height.toInt(), pitch.toInt())
            }
        }
        videoRefreshCb = cb
        libRetro.retro_set_video_refresh(cb)
    }

    fun setEnvironment(callback: EnvironmentCallback) {
        val cb = object : LibRetro.retro_environment_t {
            override fun invoke(cmd: UnsignedInt, data: Pointer): Boolean {
                when (cmd.toInt()) {
                    LibRetro.RETRO_ENVIRONMENT_SET_VARIABLES -> {
                        val variables = mutableMapOf<String, String>()
                        var offset = 0L
                        while (true) {
                            val v = retro_variable(data.share(offset))
                            variables[v.key ?: break] = v.value ?: break
                            offset += v.size()
                        }
                        callback.onSetVariables(variables)
                        return true
                    }
                    LibRetro.RETRO_ENVIRONMENT_GET_LOG_INTERFACE -> {
                        val logInterface = callback.onGetLogInterface()
                        val logCb = object : LibRetro.retro_log_printf_t {
                            override fun invoke(log_level: Int, fmt: Pointer) {
                                // FIXME: fmt is varargs
                                val message = fmt.getString(0)
                                val newLevel = when (log_level) {
                                    LibRetro.retro_log_level.RETRO_LOG_DEBUG -> LogLevel.DEBUG
                                    LibRetro.retro_log_level.RETRO_LOG_INFO -> LogLevel.INFO
                                    LibRetro.retro_log_level.RETRO_LOG_WARN -> LogLevel.WARN
                                    LibRetro.retro_log_level.RETRO_LOG_ERROR -> LogLevel.ERROR
                                    else -> throw IllegalArgumentException()
                                }
                                logInterface.onLogMessage(newLevel, message)
                            }
                        }

                        logPrintfCb = logCb

                        val logCallback = LibRetro.retro_log_callback(data)
                        logCallback.log = logCb
                        logCallback.write()

                        return true
                    }
                    LibRetro.RETRO_ENVIRONMENT_SET_SUPPORT_ACHIEVEMENTS -> {
                        val supportsAchievements = data.getByte(0).toInt() == 1
                        callback.onSetSupportAchievements(supportsAchievements)
                        return true
                    }
                    LibRetro.RETRO_ENVIRONMENT_SET_PERFORMANCE_LEVEL -> {
                        callback.onSetPerformanceLevel(data.getInt(0))
                        return true
                    }
                    LibRetro.RETRO_ENVIRONMENT_GET_VARIABLE -> {
                        val variable = retro_variable(data)
                        variable.value = callback.onGetVariable(variable.key!!)
                        return variable.value != null
                    }
                    LibRetro.RETRO_ENVIRONMENT_GET_VARIABLE_UPDATE -> {
                        val updated = callback.onGetVariableUpdate()
                        data.setByte(0, if (updated) 1 else 0)
                        return updated
                    }
                    LibRetro.RETRO_ENVIRONMENT_SET_PIXEL_FORMAT -> {
                        val pixelFormat = data.getInt(0)
                        return callback.onSetPixelFormat(pixelFormat)
                    }
                    LibRetro.RETRO_ENVIRONMENT_SET_INPUT_DESCRIPTORS -> {
                        val descriptors = mutableListOf<InputDescriptor>()
                        var offset = 0L
                        while (true) {
                            val descriptor = LibRetro.retro_input_descriptor(data.share(offset))
                            descriptor.description ?: break
                            descriptors.add(InputDescriptor(
                                    descriptor.port!!.toInt(),
                                    Device.fromValue(descriptor.device!!.toInt()),
                                    descriptor.index!!.toInt(),
                                    DeviceId.fromValue(descriptor.id!!.toInt()),
                                    descriptor.description!!))

                            offset += descriptor.size()
                        }
                        callback.onSetInputDescriptors(descriptors.toList())
                        return true
                    }
                    LibRetro.RETRO_ENVIRONMENT_SET_CONTROLLER_INFO -> {
                        val infos = mutableListOf<ControllerInfo>()
                        var offset = 0L
                        while (true) {
                            val info = LibRetro.retro_controller_info(data.share(offset))
                            info.types ?: break
                            val types = LibRetro.retro_controller_description(info.types)
                                    .toArray(info.num_types!!.toInt())
                                    .map {
                                        (it as LibRetro.retro_controller_description).let { desc ->
                                            ControllerDescription(
                                                    desc.desc!!,
                                                    Device.fromValue(desc.id!!.toInt())
                                            )
                                        }
                                    }
                            infos.add(ControllerInfo(types))
                            offset += info.size()
                        }
                        callback.onSetControllerInfo(infos.toList())
                        return true
                    }
                    LibRetro.RETRO_ENVIRONMENT_SET_MEMORY_MAPS -> {
                        callback.onSetMemoryMaps()
                        return false
                    }
                    else -> {
                        System.out.println("UNKNOWN ENV CMD!! $cmd $data")
                        return false
                    }
                }
            }
        }
        envCb = cb
        libRetro.retro_set_environment(cb)
    }

    fun setAudioSample(callback: AudioSampleCallback) {
        val cb = object : LibRetro.retro_audio_sample_t {
            override fun apply(left: Short, right: Short) {
                callback.onAudioSample(left, right)
            }
        }
        audioSampleCb = cb
        libRetro.retro_set_audio_sample(cb)
    }

    fun setAudioSampleBatch(callback: AudioSampleBatchCallback) {
        val cb = object : LibRetro.retro_audio_sample_batch_t {
            override fun apply(data: Pointer, frames: SizeT): SizeT {
                // Each frame is 4 bytes (16-bit stereo)
                val buffer = data.getByteArray(0L, frames.toInt() * 4)
                return SizeT(callback.onAudioSampleBatch(buffer, frames.toInt()))
            }
        }
        audioSampleBatchCb = cb
        libRetro.retro_set_audio_sample_batch(cb)
    }

    fun setInputPoll(callback: InputPollCallback) {
        val cb = object : LibRetro.retro_input_poll_t {
            override fun apply() {
                callback.onInputPoll()
            }
        }
        inputPollCb = cb
        libRetro.retro_set_input_poll(cb)
    }

    fun setInputState(callback: InputStateCallback) {
        val cb = object : LibRetro.retro_input_state_t {
            override fun apply(port: UnsignedInt, device: UnsignedInt, index: UnsignedInt, id: UnsignedInt): Short {
                return if (callback.onInputState(port.toInt(), device.toInt(), index.toInt(), id.toInt())) 1 else 0
            }
        }
        inputStateCb = cb
        libRetro.retro_set_input_state(cb)
    }

    fun init() {
        libRetro.retro_init()
    }

    fun deinit() {
        libRetro.retro_deinit()
    }

    fun loadGame(filePath: String): Boolean {
        val info = LibRetro.retro_game_info()
        info.path = filePath
        info.write()
        return libRetro.retro_load_game(info)
    }

    fun unloadGame() {
        libRetro.retro_unload_game()
    }

    fun run() {
        libRetro.retro_run()
    }
}
