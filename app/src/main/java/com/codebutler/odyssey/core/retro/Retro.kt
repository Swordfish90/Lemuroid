package com.codebutler.odyssey.core.retro

import android.util.Log
import com.codebutler.odyssey.SizeT
import com.codebutler.odyssey.UnsignedInt
import com.codebutler.odyssey.core.retro.LibRetro.retro_system_info
import com.codebutler.odyssey.core.retro.LibRetro.retro_variable
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.ShortByReference

class Retro(coreLibraryName: String) {

    private val libRetro : LibRetro = Native.loadLibrary(
            coreLibraryName,
            LibRetro::class.java)

    private val logPrintf = object : LibRetro.retro_log_printf_t {
        override fun invoke(log_level: Int, fmt: Pointer) {
            // FIXME: fmt is varargs
            val message = fmt.getString(0)
            val level = LibRetro.retro_log_level.values().find { it.value == log_level }
                    ?: LibRetro.retro_log_level.RETRO_LOG_DUMMY
            logCallback?.invoke(level, message)
        }
    }

    var logCallback: ((level: LibRetro.retro_log_level, message: String) -> Unit)? = null

    // FIXME: Don't expose Pointer here
    var videoCallback: ((data: Pointer, width: Int, height: Int, pitch: Int) -> Unit)? = null

    data class ControllerDescription(
            val desc: String,
            val id: RetroDevice
    )

    data class ControllerInfo(
            val types: List<ControllerDescription>
    )

    data class SystemInfo(
            val libraryName: String,
            val libraryVersion: String,
            val validExtensions: String,
            val needFullpath: Boolean,
            val blockExtract: Boolean
    )

    data class InputDescriptor(
            val port: Int,
            val device: RetroDevice,
            val index: Int,
            val id: RetroDeviceId,
            val description: String
    )

    enum class RetroDevice(val value: Int) {
        RETRO_DEVICE_NONE(0),
        RETRO_DEVICE_JOYPAD(1),
        RETRO_DEVICE_MOUSE(2),
        RETRO_DEVICE_KEYBOARD(3),
        RETRO_DEVICE_LIGHTGUN(4),
        RETRO_DEVICE_ANALOG(5),
        RETRO_DEVICE_POINTER(6),

        // FIXME: What's the deal with these...
        RETRO_DEVICE_JOYPAD_MULTITAP ((1 shl 8) or RETRO_DEVICE_JOYPAD.value),
        RETRO_DEVICE_LIGHTGUN_SUPER_SCOPE ((1 shl 8) or RETRO_DEVICE_LIGHTGUN.value),
        RETRO_DEVICE_LIGHTGUN_JUSTIFIER ((2 shl 8) or RETRO_DEVICE_LIGHTGUN.value),
        RETRO_DEVICE_LIGHTGUN_JUSTIFIERS ((3 shl 8) or RETRO_DEVICE_LIGHTGUN.value),
    }

    enum class RetroDeviceId(val value: Int) {
        RETRO_DEVICE_ID_JOYPAD_B(0),
        RETRO_DEVICE_ID_JOYPAD_Y(1),
        RETRO_DEVICE_ID_JOYPAD_SELECT(2),
        RETRO_DEVICE_ID_JOYPAD_START(3),
        RETRO_DEVICE_ID_JOYPAD_UP(4),
        RETRO_DEVICE_ID_JOYPAD_DOWN(5),
        RETRO_DEVICE_ID_JOYPAD_LEFT(6),
        RETRO_DEVICE_ID_JOYPAD_RIGHT(7),
        RETRO_DEVICE_ID_JOYPAD_A(8),
        RETRO_DEVICE_ID_JOYPAD_X(9),
        RETRO_DEVICE_ID_JOYPAD_L(10),
        RETRO_DEVICE_ID_JOYPAD_R(11),
        RETRO_DEVICE_ID_JOYPAD_L2(12),
        RETRO_DEVICE_ID_JOYPAD_R2(13),
        RETRO_DEVICE_ID_JOYPAD_L3(14),
        RETRO_DEVICE_ID_JOYPAD_R3(15)
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

    interface Callback {

        fun onSetVariables(variables: Map<String, String>)

        fun onSetSupportAchievements(supportsAchievements: Boolean)

        fun onSetPerformanceLevel(performanceLevel: Int)

        fun onGetVariable(name: String): String?

        // FIXME: Don't expose LibRetro.* ... ?
        fun onSetPixelFormat(retroPixelFormat: LibRetro.retro_pixel_format): Boolean

        fun onSetInputDescriptors(descriptors: List<InputDescriptor>)

        fun onSetControllerInfo(info: List<ControllerInfo>)

        fun onGetVariableUpdate(): Boolean

        fun onSetMemoryMaps()
    }

    fun setEnvironment(callback: Callback) {
        libRetro.retro_set_environment(object : LibRetro.retro_environment_t {
            override fun invoke(cmd: UnsignedInt, data: Pointer): Boolean {
                when (cmd.toLong()) { // FIXME: Reorder
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
                        val logCallback = LibRetro.retro_log_callback(data)
                        logCallback.log = logPrintf
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
                        val pixelFormatInt = data.getInt(0)
                        val pixelFormat = LibRetro.retro_pixel_format.values().find { it.value == pixelFormatInt }
                                ?: LibRetro.retro_pixel_format.RETRO_PIXEL_FORMAT_UNKNOWN
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
                                    RetroDevice.values().find { it.value == descriptor.device!!.toInt() }!!,
                                    descriptor.index!!.toInt(),
                                    RetroDeviceId.values().find { it.value == descriptor.id!!.toInt() }!!,
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
                                                    RetroDevice.values().find { it.value == desc.id!!.toInt() }!!
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
        })
    }

    private val videoRefresh = object : LibRetro.retro_video_refresh_t {
        override fun invoke(data: Pointer, width: UnsignedInt, height: UnsignedInt, pitch: SizeT) {
            videoCallback?.invoke(data, width.toInt(), height.toInt(), pitch.toInt())
        }
    }

    private val audioSampleCb = object : LibRetro.retro_audio_sample_t {
        override fun apply(left: Short, right: Short) {
            Log.d("Retro", "AUDIO SAMPLE!!")
        }
    }

    private val audioSampleBatchCb = object : LibRetro.retro_audio_sample_batch_t {
        override fun apply(data: ShortByReference, frames: SizeT): SizeT {
            Log.d("RETRO", "AUDIO SAMPLE BATCH!!")
            return SizeT(0)
        }
    }

    private val inputPollCb = object : LibRetro.retro_input_poll_t {
        override fun apply() {
            Log.d("RETRO", "INPUT POLL")
        }
    }

    private val inputStateCb = object : LibRetro.retro_input_state_t {
        override fun apply(port: UnsignedInt, device: UnsignedInt, index: UnsignedInt, id: UnsignedInt): Short {
            Log.d("RETRO", "INPUT STATE")
            return 0
        }
    }

    // FIXME: Pass in callbacks to these methods...

    fun setVideoRefresh() {
        libRetro.retro_set_video_refresh(videoRefresh)
    }

    fun setAudioSample() {
        libRetro.retro_set_audio_sample(audioSampleCb)
    }

    fun setAudioSampleBatch() {
        libRetro.retro_set_audio_sample_batch(audioSampleBatchCb)
    }

    fun setInputPoll() {
        libRetro.retro_set_input_poll(inputPollCb)
    }

    fun setInputState() {
        libRetro.retro_set_input_state(inputStateCb)
    }

    fun init() {
        libRetro.retro_init()
    }

    fun deinit() {
        libRetro.retro_deinit()
    }

    fun loadGame(): Boolean {
        val info = LibRetro.retro_game_info()
        info.path = "/sdcard/Super Mario All-Stars (U) [!].smc"
        info.write()
        return libRetro.retro_load_game(info)
    }

    fun run() {
        libRetro.retro_run()
    }

    fun getSystemAVInfo(): LibRetro.retro_system_av_info {
        val info = LibRetro.retro_system_av_info()
        libRetro.retro_get_system_av_info(info)
        //info.read() // FIXME: Needed?
        return info
    }

    enum class Region(val value: Int) {
        RETRO_REGION_NTSC(0),
        RETRO_REGION_PAL(1)
    }

    fun getRegion(): Region {
        val region = libRetro.retro_get_region()
        return Region.values().find { it.value == region.toInt() }
                ?: throw Exception("Unknown Region: $region")
    }
}
