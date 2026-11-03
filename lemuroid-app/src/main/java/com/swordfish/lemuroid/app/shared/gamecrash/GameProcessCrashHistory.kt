package com.swordfish.lemuroid.app.shared.gamecrash

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/**
 * Recovers crash information left by Android when the isolated :game process dies.
 * This must run in the main process after the game process has terminated.
 */
object GameProcessCrashHistory {
    private const val PREFS_NAME = "game_process_crash_history"
    private const val KEY_LAST_PROCESSED_TIMESTAMP = "last_processed_timestamp"
    private const val KEY_PENDING_DETAIL = "pending_detail"
    private const val MAX_TRACE_BYTES = 64 * 1024

    fun collect(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return

        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastProcessedTimestamp = preferences.getLong(KEY_LAST_PROCESSED_TIMESTAMP, 0L)
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return

        val exits =
            try {
                activityManager.getHistoricalProcessExitReasons(context.packageName, null, 20)
            } catch (_: RuntimeException) {
                return
            }

        val gameProcessName = "${context.packageName}:game"
        val exitInfo =
            exits.firstOrNull { info ->
                info.processName == gameProcessName &&
                    info.timestamp > lastProcessedTimestamp &&
                    isUnexpectedExit(info.reason)
            } ?: run {
                val newestTimestamp = exits.firstOrNull { it.processName == gameProcessName }?.timestamp ?: 0L
                if (newestTimestamp > lastProcessedTimestamp) {
                    preferences.edit().putLong(KEY_LAST_PROCESSED_TIMESTAMP, newestTimestamp).apply()
                }
                return
            }

        val detail = buildString {
            appendLine("Falha recuperada pelo Android")
            appendLine("Processo: ${exitInfo.processName}")
            appendLine("Data/hora (epoch ms): ${exitInfo.timestamp}")
            appendLine("Motivo: ${reasonName(exitInfo.reason)} (${exitInfo.reason})")
            appendLine("Status: ${exitInfo.status}")
            appendLine("Descrição: ${exitInfo.description ?: "não informada"}")
            appendLine("Importância do processo: ${exitInfo.importance}")
            appendLine("Memória PSS: ${exitInfo.pss} KB")
            appendLine("Memória RSS: ${exitInfo.rss} KB")

            val trace = readTrace(exitInfo)
            if (trace.isNotBlank()) {
                appendLine()
                appendLine("Rastreamento fornecido pelo Android:")
                append(trace)
            } else {
                appendLine()
                append("O Android não disponibilizou um rastreamento detalhado para esta falha.")
            }
        }

        preferences.edit()
            .putLong(KEY_LAST_PROCESSED_TIMESTAMP, exitInfo.timestamp)
            .putString(KEY_PENDING_DETAIL, detail)
            .apply()
    }

    fun consumePendingDetail(context: Context): String? {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val detail = preferences.getString(KEY_PENDING_DETAIL, null)
        if (detail != null) {
            preferences.edit().remove(KEY_PENDING_DETAIL).apply()
        }
        return detail
    }

    private fun isUnexpectedExit(reason: Int): Boolean =
        reason == ApplicationExitInfo.REASON_CRASH_NATIVE ||
            reason == ApplicationExitInfo.REASON_CRASH ||
            reason == ApplicationExitInfo.REASON_ANR ||
            reason == ApplicationExitInfo.REASON_SIGNALED

    private fun reasonName(reason: Int): String =
        when (reason) {
            ApplicationExitInfo.REASON_CRASH_NATIVE -> "CRASH_NATIVE"
            ApplicationExitInfo.REASON_CRASH -> "CRASH"
            ApplicationExitInfo.REASON_ANR -> "ANR"
            ApplicationExitInfo.REASON_SIGNALED -> "SIGNALED"
            else -> "UNKNOWN"
        }

    private fun readTrace(exitInfo: ApplicationExitInfo): String {
        return try {
            val input = exitInfo.traceInputStream ?: return ""
            input.use { stream ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                while (output.size() < MAX_TRACE_BYTES) {
                    val remaining = MAX_TRACE_BYTES - output.size()
                    val count = stream.read(buffer, 0, minOf(buffer.size, remaining))
                    if (count <= 0) break
                    output.write(buffer, 0, count)
                }
                String(output.toByteArray(), StandardCharsets.UTF_8)
            }
        } catch (_: Exception) {
            ""
        }
    }
}
