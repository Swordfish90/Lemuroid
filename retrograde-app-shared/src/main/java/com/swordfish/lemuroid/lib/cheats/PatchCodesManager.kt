package com.swordfish.lemuroid.lib.cheats

import android.content.Context
import android.net.Uri
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.PatchCode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PatchCodesManager @Inject constructor(
    private val retrogradeDatabase: RetrogradeDatabase,
) {
    fun getCodesForGame(gameId: Int): Flow<List<PatchCode>> =
        retrogradeDatabase.patchCodeDao().getCodesForGame(gameId)

    suspend fun getAllCodesForGame(gameId: Int): List<PatchCode> =
        retrogradeDatabase.patchCodeDao().getAllCodesForGame(gameId)

    suspend fun getEnabledCodesForGame(gameId: Int): List<PatchCode> =
        retrogradeDatabase.patchCodeDao().getEnabledCodesForGame(gameId)

    suspend fun saveCode(code: PatchCode): Long =
        retrogradeDatabase.patchCodeDao().insert(code)

    suspend fun updateCode(code: PatchCode) =
        retrogradeDatabase.patchCodeDao().update(code)

    suspend fun deleteCode(code: PatchCode) =
        retrogradeDatabase.patchCodeDao().delete(code)

    suspend fun importFromUri(context: Context, uri: Uri, gameId: Int): List<PatchCode> {
        val lines = readLines(context, uri)
        val parsed = parseLines(lines, gameId)
        if (parsed.isEmpty()) throw ImportException("No valid codes found in the file.")
        parsed.forEach { retrogradeDatabase.patchCodeDao().insert(it) }
        return parsed
    }

    private fun readLines(context: Context, uri: Uri): List<String> {
        return try {
            context.contentResolver.openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readLines() }
                ?: throw ImportException("Cannot open file.")
        } catch (e: ImportException) {
            throw e
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

        val countStr = props["cheats"] ?: return emptyList()
        val count = countStr.toIntOrNull() ?: return emptyList()

        return (0 until count).mapNotNull { i ->
            val code = props["cheat${i}_code"] ?: return@mapNotNull null
            if (code.isBlank()) return@mapNotNull null
            val desc = props["cheat${i}_desc"] ?: ""
            val enabled = props["cheat${i}_enable"]?.lowercase() == "true"
            PatchCode(
                gameId = gameId,
                description = desc.trim(),
                code = code.trim().uppercase(),
                enabled = enabled,
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
                            code = trimmed.uppercase(),
                            enabled = false,
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
