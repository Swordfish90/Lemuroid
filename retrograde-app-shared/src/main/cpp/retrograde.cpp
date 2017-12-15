/*
 * retrograde.cpp
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
#include <jni.h>
#include <stdio.h>
#include <stdarg.h>
#include <limits.h>

extern "C" {
    enum retro_log_level {
        RETRO_LOG_DUMMY = INT_MAX
    };

    typedef void (*retro_log_printf_t)(
            enum retro_log_level level,
            const char *fmt,
            ...);

    typedef void (*retrograde_log_printf_t)(
            enum retro_log_level level,
            const char *fmt,
            va_list args);

    static retrograde_log_printf_t retrograde_log_cb;

    void _retro_log_printf(enum retro_log_level level, const char *fmt, ...) {
        va_list args;
        va_start(args, fmt);
        retrograde_log_cb(level, fmt, args);
        va_end(args);
    }

    JNIEXPORT
    void retrograde_set_log_callback(retrograde_log_printf_t cb) {
        retrograde_log_cb = cb;
    }

    JNIEXPORT
    retro_log_printf_t retrograde_get_retro_log_printf() {
        return _retro_log_printf;
    }

    JNIEXPORT
    void retrograde_redirect_stdio(char *stdout_path, char* stderr_path) {
        freopen(stdout_path, "w", stdout);
        freopen(stderr_path, "w", stderr);

        setvbuf(stdout, NULL, _IONBF, 0);
        setvbuf(stderr, NULL, _IONBF, 0);
    }
}
