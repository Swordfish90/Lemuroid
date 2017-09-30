/*
 * SizeT.kt
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

package com.codebutler.odyssey.common.jna;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;

// FIXME: Can't seem to convert this to Kotlin?

public class SizeT extends IntegerType {

    public SizeT() {
        this(0);
    }

    public SizeT(long value) {
        super(Native.SIZE_T_SIZE, value, true);
    }
}
