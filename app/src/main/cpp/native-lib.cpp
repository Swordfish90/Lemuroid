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

    typedef void (*odyssey_log_printf_t)(
            enum retro_log_level level,
            const char *fmt,
            va_list args);

    static odyssey_log_printf_t odyssey_log_cb;

    void _retro_log_printf(enum retro_log_level level, const char *fmt, ...) {
        va_list args;
        va_start(args, fmt);
        odyssey_log_cb(level, fmt, args);
        va_end(args);
    }

    JNIEXPORT
    void odyssey_set_log_callback(odyssey_log_printf_t cb) {
        odyssey_log_cb = cb;
    }

    JNIEXPORT
    retro_log_printf_t odyssey_get_retro_log_printf() {
        return _retro_log_printf;
    }
}
