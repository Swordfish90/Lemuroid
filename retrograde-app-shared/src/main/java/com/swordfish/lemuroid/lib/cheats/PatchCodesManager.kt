package com.swordfish.lemuroid.lib.cheats

import android.content.Context
import android.net.Uri
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.PatchCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class PatchCodesManager @Inject constructor(
    private val retrogradeDatabase: RetrogradeDatabase,
) {

    private val mutex = Mutex()

    fun getCodesForGame(gameId: Int): Flow<List<PatchCode>> =
        retrogradeDatabase.patchCodeDao().getCodesForGame(gameId)

    suspend fun getEnabledCodesForGame(gameId: Int): List<PatchCode> =
        retrogradeDatabase.patchCodeDao().getEnabledCodesForGame(gameId)

    suspend fun saveCode(code: PatchCode): Long = mutex.withLock {
        val normalized = normalize(code)
        // prevent duplicates
        val existing = retrogradeDatabase.patchCodeDao()
            .getAllCodesForGame(code.gameId)
            .find { it.code == normalized.code }

        if (existing != null) {
            retrogradeDatabase.patchCodeDao().update(existing.copy(
                description = normalized.description,
                enabled = normalized.enabled
            ))
            existing.id
        } else {
            retrogradeDatabase.patchCodeDao().insert(normalized)
        }
    }

    suspend fun toggleCode(code: PatchCode) = mutex.withLock {
        val latest = retrogradeDatabase.patchCodeDao()
            .getAllCodesForGame(code.gameId)
            .find { it.id == code.id } ?: return

        val updated = latest.copy(enabled = !latest.enabled)
        retrogradeDatabase.patchCodeDao().update(updated)
    }

    suspend fun updateCode(code: PatchCode) = mutex.withLock {
        retrogradeDatabase.patchCodeDao().update(normalize(code))
    }

    suspend fun deleteCode(code: PatchCode) = mutex.withLock {
        retrogradeDatabase.patchCodeDao().delete(code)
    }

    suspend fun importFromUri(context: Context, uri: Uri, gameId: Int): List<PatchCode> {
        val lines = readLines(context, uri)
        val parsed = parseLines(lines, gameId)
        if (parsed.isEmpty()) throw ImportException("No valid codes found in the file.")

        return mutex.withLock {
            val existing = retrogradeDatabase.patchCodeDao().getAllCodesForGame(gameId)
            val existingCodes = existing.map { it.code }.toSet()

            val filtered = parsed
                .map { normalize(it) }
                .filter { it.code !in existingCodes }

            filtered.forEach { retrogradeDatabase.patchCodeDao().insert(it) }
            filtered
        }
    }

    private fun normalize(code: PatchCode): PatchCode {
        return code.copy(
            description = code.description.trim(),
            code = code.code.replace(" ", "").uppercase()
        )
    }

    private fun readLines(context: Context, uri: Uri): List<String> {
        return try {
            context.contentResolver.openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readLines() }
                ?: throw ImportException("Cannot open file.")
        } catch (e: Exception) {
            throw ImportException("Failed to read file: ${e.message}")
        }
    }

    private fun parseLines(lines: List<String>, gameId: Int): List<PatchCode> {
        val isCht = lines.any { it.trimStart().startsWith("cheats") || it.contains("_code") }
        return if (isCht) parseCht(lines, gameId) else parseTxt(lines, gameId)
    }

    private fun parseCht(lines: List<String>, gameId: Int): List<PatchCode> {
        val props = mutableMapOf<String, String>()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
            val eq = trimmed.indexOf('=')
            if (eq < 0) continue
            val key = trimmed.substring(0, eq).trim()
            val value = trimmed.substring(eq + 1).trim().removeSurrounding("\"")
            props[key] = value
        }

        val count = props["cheats"]?.toIntOrNull() ?: return emptyList()

        return (0 until count).mapNotNull { i ->
            val code = props["cheat${i}_code"] ?: return@mapNotNull null
            if (code.isBlank()) return@mapNotNull null

            PatchCode(
                gameId = gameId,
                description = props["cheat${i}_desc"]?.trim() ?: "",
                code = code,
                enabled = props["cheat${i}_enable"]?.lowercase() == "true",
            )
        }
    }

    private fun parseTxt(lines: List<String>, gameId: Int): List<PatchCode> {
        val result = mutableListOf<PatchCode>()
        var pendingDesc = ""
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> pendingDesc = ""
                trimmed.startsWith("#") -> pendingDesc = trimmed.removePrefix("#").trim()
                else -> {
                    result.add(
                        PatchCode(
                            gameId = gameId,
                            description = pendingDesc,
                            code = trimmed,
                            enabled = true,
                        ),
                    )
                    pendingDesc = ""
                }
            }
        }
        return result
    }

    class ImportException(message: String) : Exception(message)
}
