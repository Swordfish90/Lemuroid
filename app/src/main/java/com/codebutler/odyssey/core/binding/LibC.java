/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2016 Caprica Software Limited.
 */

package com.codebutler.odyssey.core.binding;

import java.nio.ByteBuffer;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

/**
 * Minimal interface to the standard "C" library.
 */
public interface LibC extends Library {

    /**
     * Native library instance.
     */
    LibC INSTANCE = (LibC)Native.loadLibrary((Platform.isWindows() ? "msvcrt" : "c"), LibC.class);

    /**
     * Format a string with a variable arguments list into a fixed size buffer.
     * <p>
     * The resultant string will be truncated to the size of the buffer if it would
     * otherwise exceed it.
     * <p>
     * For example, if the buffer has a capacity of 10 this is a maximum of 9
     * characters plus a null terminator for a total capacity of 10. This means if
     * 10 characters are required, the buffer capacity must be 11 to accommodate
     * the null terminator.
     *
     * @param str buffer
     * @param size capacity of the buffer, including space for a null terminator
     * @param format format string
     * @param args format arguments
     * @return length of the formatted string, which may exceed the capacity of the buffer, or less than zero on error
     */
    int vsnprintf(ByteBuffer str, int size, String format, Pointer args);

    /**
     * Locks (pins) parts of virtual address space into RAM so it can not be swapped out.
     *
     * @param addr address pointer
     * @param length length
     * @return 0 if successful; -1 if not, setting <code>errno</code> to an error code
     */
    int mlock(Pointer addr, NativeLong length);

    /**
     * Unlock previously locked memory.
     *
     * @param addr address pointer
     * @param length length
     * @return 0 if successful; -1 if not, setting <code>errno</code> to an error code
     */
    int munlock(Pointer addr, NativeLong length);

    /**
     * Open a stream for a file descriptor.
     *
     * @param filedes file descriptor
     * @param mode open mode
     * @return file descriptor; or <code>NULL</code>, setting <code>errno</code> to an error code
     */
    Pointer fdopen(int filedes, String mode);

    /**
     * Associate an existing stream to a new file.
     *
     * @param path new file path
     * @param mode open mode
     * @param stream file stream
     * @return file descriptor; or <code>NULL</code>, setting <code>errno</code> to an error code
     */
    Pointer freopen(String path, String mode, Pointer stream);

    /**
     * Close a file stream.
     *
     * @param stream stream
     * @return 0 if successful; -1 if not, setting <code>errno</code> to an error code
     */
    int fclose(Pointer stream);

    /**
     * Change or add an evironment variable.
     * <p>
     * The value strings are copied (natively).
     * <p>
     * <em>Not available on Windows.</em>
     *
     * @param name name of environment variable
     * @param value value of the environment variable
     * @param overwrite non-zero to replace any existing value
     * @return 0 if successful; -1 if not, setting <code>errno</code> to an error code
     */
    int setenv(String name, String value, int overwrite);

    /**
     * Unset an environment variable.
     * <p>
     * <em>Not available on Windows.</em>
     *
     * @param name name of environment variable
     * @return 0 if successful; -1 if not, setting <code>errno</code> to an error code
     */
    int unsetenv(String name);

    /**
     * Get the current process id.
     * <p>
     * <em>Not available on Windows.</em>
     *
     * @return process id
     */
    int getpid();

    /**
     * Closest Windows equivalent to {@link #setenv(String, String, int)}.
     * <p>
     * Note that after setting an environment variable, it will <em>not</em> show up via
     * System#getenv even if it was successfully set.
     * <p>
     * Use with case, it is not guaranteed to be thread-safe.
     * <p>
     * <em>Only available on Windows.</em>
     *
     * @param envstring variable and value to set, in the format "variable=value", without quotes.
     * @return zero on success, non-zero on error
     */
    int _putenv(String envstring);
}

