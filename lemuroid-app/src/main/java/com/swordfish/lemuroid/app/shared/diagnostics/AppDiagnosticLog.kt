package com.swordfish.lemuroid.app.shared.diagnostics

import android.content.Context
import android.os.Process
import timber.log.Timber
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Small persistent diagnostic journal. It captures Timber events from both the main
 * and isolated :game process, allowing native/core failures to be correlated later.
 */
object AppDiagnosticLog {
    private const val FILE_NAME = "lemuroid-diagnostics.log"
    private const val MAX_BYTES = 2L * 1024L * 1024L
    private const val KEEP_BYTES = 1536L * 1024L

    private val timestampFormat = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    }

    fun file(context: Context): File = File(context.applicationContext.filesDir, FILE_NAME)

    fun read(context: Context, maxBytes: Int = 512 * 1024): String {
        val logFile = file(context)
        if (!logFile.exists()) return "Nenhum registro capturado ainda."
        return runCatching {
            RandomAccessFile(logFile, "r").use { input ->
                val start = (input.length() - maxBytes).coerceAtLeast(0L)
                input.seek(start)
                val bytes = ByteArray((input.length() - start).toInt())
                input.readFully(bytes)
                String(bytes, StandardCharsets.UTF_8)
            }
        }.getOrElse { "Falha ao ler o registro: ${it.stackTraceToString()}" }
    }

    fun clear(context: Context): Boolean = runCatching {
        file(context).writeText("", StandardCharsets.UTF_8)
    }.isSuccess

    fun append(
        context: Context,
        level: String,
        tag: String?,
        message: String,
        throwable: Throwable? = null,
    ) {
        runCatching {
            val target = file(context)
            val process = if (Process.is64Bit()) "64-bit" else "32-bit"
            val line = buildString {
                append(timestampFormat.get().format(Date()))
                append(" [").append(level).append("] [").append(process).append("] ")
                append(tag ?: "Lemuroid").append(": ").append(message)
                appendLine()
                throwable?.let { appendLine(it.stackTraceToString()) }
            }
            val bytes = line.toByteArray(StandardCharsets.UTF_8)
            RandomAccessFile(target, "rw").use { output ->
                output.channel.lock().use {
                    if (output.length() > MAX_BYTES) {
                        val keep = minOf(output.length(), KEEP_BYTES)
                        val tail = ByteArray(keep.toInt())
                        output.seek(output.length() - keep)
                        output.readFully(tail)
                        output.setLength(0)
                        output.seek(0)
                        output.write(tail)
                    }
                    output.seek(output.length())
                    output.write(bytes)
                }
            }
        }
    }

    class PersistentTree(context: Context) : Timber.Tree() {
        private val appContext = context.applicationContext

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val level = when (priority) {
                android.util.Log.ERROR, android.util.Log.ASSERT -> "ERROR"
                android.util.Log.WARN -> "WARN"
                android.util.Log.INFO -> "INFO"
                android.util.Log.DEBUG -> "DEBUG"
                else -> "VERBOSE"
            }
            append(appContext, level, tag, message, t)
        }
    }
}
