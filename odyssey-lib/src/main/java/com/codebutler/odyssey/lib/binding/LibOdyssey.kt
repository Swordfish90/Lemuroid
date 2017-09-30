/*
 * LibOdyssey.kt
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

package com.codebutler.odyssey.lib.binding

import com.codebutler.odyssey.lib.retro.LibRetro
import com.sun.jna.Library
import com.sun.jna.Native

interface LibOdyssey : Library {

    companion object {
        val INSTANCE = Native.loadLibrary("odyssey", LibOdyssey::class.java)
    }

    fun odyssey_set_log_callback(cb: LibRetro.retro_log_printf_t)

    fun odyssey_get_retro_log_printf(): LibRetro.retro_log_printf_t
}
