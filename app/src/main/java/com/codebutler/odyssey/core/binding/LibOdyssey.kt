package com.codebutler.odyssey.core.binding

import com.sun.jna.Library
import com.sun.jna.Native

interface LibOdyssey: Library {

    companion object {
        val INSTANCE = Native.loadLibrary("odyssey", LibOdyssey::class.java)
    }

    fun odyssey_set_log_callback(cb: LibRetro.retro_log_printf_t)

    fun odyssey_get_retro_log_printf(): LibRetro.retro_log_printf_t
}
