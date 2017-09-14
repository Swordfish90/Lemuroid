/*
 * LibRetro.kt
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

package com.codebutler.odyssey.core.retro.lib

import com.codebutler.odyssey.core.jna.SizeT
import com.codebutler.odyssey.core.jna.UnsignedInt
import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Pointer
import com.sun.jna.Structure

/**
 * Based on libretro.h
 */
interface LibRetro : Library {

    companion object {
        const val RETRO_ENVIRONMENT_EXPERIMENTAL = 0x10000

        const val RETRO_ENVIRONMENT_SET_VARIABLES = 16
        const val RETRO_ENVIRONMENT_GET_LOG_INTERFACE = 27
        const val RETRO_ENVIRONMENT_SET_SUPPORT_ACHIEVEMENTS = 42 or RETRO_ENVIRONMENT_EXPERIMENTAL
        const val RETRO_ENVIRONMENT_SET_MEMORY_MAPS = 36 or RETRO_ENVIRONMENT_EXPERIMENTAL
        const val RETRO_ENVIRONMENT_SET_PERFORMANCE_LEVEL = 8
        const val RETRO_ENVIRONMENT_GET_VARIABLE = 15
        const val RETRO_ENVIRONMENT_GET_VARIABLE_UPDATE = 17
        const val RETRO_ENVIRONMENT_SET_PIXEL_FORMAT = 10
        const val RETRO_ENVIRONMENT_SET_INPUT_DESCRIPTORS = 11
        const val RETRO_ENVIRONMENT_SET_CONTROLLER_INFO = 35
    }

    object retro_pixel_format {
        /** 0RGB1555, native endian.
         * 0 bit must be set to 0.
         * This pixel format is default for compatibility concerns only.
         * If a 15/16-bit pixel format is desired, consider using RGB565. */
        const val RETRO_PIXEL_FORMAT_0RGB1555 = 0

        /** XRGB8888, native endian.
         * X bits are ignored. */
        const val RETRO_PIXEL_FORMAT_XRGB8888 = 1

        /** RGB565, native endian.
         * This pixel format is the recommended format to use if a 15/16-bit
         * format is desired as it is the pixel format that is typically
         * available on a wide range of low-power devices.
         *
         * It is also natively supported in APIs like OpenGL ES. */
        const val RETRO_PIXEL_FORMAT_RGB565 = 2
    }

    object retro_log_level {
        const val RETRO_LOG_DEBUG = 0
        const val RETRO_LOG_INFO = 1
        const val RETRO_LOG_WARN = 2
        const val RETRO_LOG_ERROR = 3
    }

    class retro_system_info : Structure() {
        @JvmField
        var library_name : String? = null

        @JvmField
        var library_version: String? = null

        @JvmField
        var valid_extensions: String? = null

        @JvmField
        var need_fullpath: Boolean = false

        @JvmField
        var block_extract: Boolean = false

        override fun getFieldOrder() = listOf(
                "library_name",
                "library_version",
                "valid_extensions",
                "need_fullpath",
                "block_extract")
    }

    class retro_game_info : Structure() {
        @JvmField
        var path: String? = null

        @JvmField
        var data: Pointer? = null

        @JvmField
        var size: SizeT = SizeT()

        @JvmField
        var meta: String? = null

        override fun getFieldOrder() = listOf(
                "path",
                "data",
                "size",
                "meta")
    }

    class retro_game_geometry(pointer: Pointer? = null) : Structure(pointer) {
        /**
         * Nominal video width of game.
         */
        @JvmField
        var base_width: UnsignedInt? = null

        /**
         * Nominal video height of game.
         */
        @JvmField
        var base_height: UnsignedInt? = null

        /**
         * possible width of game.
         */
        @JvmField
        var max_width: UnsignedInt? = null

        /**
         * Maximum possible height of game.
         */
        @JvmField
        var max_height: UnsignedInt? = null

        /**
         * Nominal aspect ratio of game. If
         * aspect_ratio is <= 0.0, an aspect ratio
         * of base_width / base_height is assumed.
         * A frontend could override this setting,
         * if desired.
         */
        @JvmField
        var aspect_ratio: Float? = null

        override fun getFieldOrder() = listOf(
                "base_width",
                "base_height",
                "max_width",
                "max_height",
                "aspect_ratio")
    }

    class retro_system_timing(pointer: Pointer? = null) : Structure(pointer) {
        @JvmField
        var fps: Double? = null

        @JvmField
        var sample_rate: Double? = null

        override fun getFieldOrder() = listOf("fps", "sample_rate")
    }

    class retro_system_av_info(pointer: Pointer? = null) : Structure(pointer) {
        @JvmField
        var geometry: retro_game_geometry? = null

        @JvmField
        var timing: retro_system_timing? = null

        override fun getFieldOrder() = listOf("geometry", "timing")
    }

    class retro_variable(p: Pointer? = null) : Structure(p) {
        @JvmField
        var key: String? = null

        @JvmField
        var value: String? = null

        init {
            read()
        }

        override fun getFieldOrder() = listOf("key", "value")
    }

    class retro_log_callback(pointer: Pointer? = null) : Structure(pointer) {

        @JvmField
        var log: retro_log_printf_t? = null

        init {
            read()
        }

        override fun getFieldOrder(): List<String> = listOf("log")
    }

    class retro_input_descriptor(pointer: Pointer? = null): Structure(pointer) {

        @JvmField
        var port: UnsignedInt? = null

        @JvmField
        var device: UnsignedInt? = null

        @JvmField
        var index: UnsignedInt? = null

        @JvmField
        var id: UnsignedInt? = null

        @JvmField
        var description: String? = null

        init {
            read()
        }

        override fun getFieldOrder(): List<String> = listOf("port", "device", "index", "id", "description")
    }

    class retro_controller_description(pointer: Pointer? = null): Structure(pointer) {

        @JvmField
        var desc: String? = null

        @JvmField
        var id: UnsignedInt? = null

        init {
            read()
        }

        override fun getFieldOrder(): List<String> = listOf("desc", "id")
    }

    class retro_controller_info(pointer: Pointer? = null): Structure(pointer) {

        @JvmField
        // FIXME: var types: retro_controller_description? = null
        var types: Pointer? = null

        @JvmField
        var num_types: UnsignedInt? = null

        init {
            read()
        }

        override fun getFieldOrder(): List<String> = listOf("types", "num_types")
    }

    interface retro_environment_t : Callback {
        fun invoke(cmd: UnsignedInt, data: Pointer) : Boolean
    }

    interface retro_log_printf_t : Callback {
        //num retro_log_level level, const char *fmt, ...
        fun invoke(log_level: Int, fmt: Pointer)
    }

    interface retro_video_refresh_t : Callback {
        fun invoke(data: Pointer, width: UnsignedInt, height: UnsignedInt, pitch: SizeT)
    }

    interface retro_audio_sample_t : Callback {
        fun apply(left: Short, right: Short)
    }

    interface retro_audio_sample_batch_t : Callback {
        fun apply(data: Pointer, frames: SizeT): SizeT
    }

    interface retro_input_poll_t : Callback {
        fun apply()
    }

    interface retro_input_state_t : Callback {
        fun apply(port: UnsignedInt, device: UnsignedInt, index: UnsignedInt, id: UnsignedInt): Short
    }

    fun retro_get_system_info(info: retro_system_info)

    fun retro_set_environment(cb: retro_environment_t)

    fun retro_set_video_refresh(cb: retro_video_refresh_t)

    fun retro_set_audio_sample(cb: retro_audio_sample_t)

    fun retro_set_audio_sample_batch(cb: retro_audio_sample_batch_t)

    fun retro_set_input_poll(cb: retro_input_poll_t)

    fun retro_set_input_state(cb: retro_input_state_t)

    fun retro_init()

    fun retro_deinit()

    fun retro_load_game(game: retro_game_info): Boolean

    fun retro_unload_game()

    fun retro_run()

    fun retro_get_system_av_info(info: retro_system_av_info)

    fun retro_get_region(): UnsignedInt

    fun retro_get_memory_data(id: UnsignedInt): Pointer

    fun retro_get_memory_size(id: UnsignedInt): SizeT
}
