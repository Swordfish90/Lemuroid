package com.swordfish.lemuroid.lib.cheats.cht

import com.swordfish.lemuroid.lib.cheats.CheatSupport
import com.swordfish.lemuroid.lib.cheats.CheatType
import com.swordfish.lemuroid.lib.cheats.CheatValidator
import com.swordfish.lemuroid.lib.cheats.ValidationResult
import com.swordfish.lemuroid.lib.library.db.dao.CheatDao
import com.swordfish.lemuroid.lib.library.db.entity.Cheat
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.io.InputStream

enum class ImportMode {
    MERGE,
    REPLACE,
}

enum class CandidateStatus {
    VALID,
    DUPLICATE,
    UNSUPPORTED,
    INVALID,
}

data class ImportCandidate(
    val entry: ChtEntry,
    val title: String,
    val code: String,
    val detectedType: CheatType?,
    val enabled: Boolean,
    val isMasterCode: Boolean,
    val status: CandidateStatus,
    val reason: String? = null,
)

data class ImportPreview(
    val fileName: String,
    val systemId: String,
    val totalEntries: Int,
    val candidates: List<ImportCandidate>,
) {
    val validEntries: List<ImportCandidate> get() = candidates.filter { it.status == CandidateStatus.VALID }
    val duplicateEntries: List<ImportCandidate> get() = candidates.filter { it.status == CandidateStatus.DUPLICATE }
    val unsupportedEntries: List<ImportCandidate> get() = candidates.filter { it.status == CandidateStatus.UNSUPPORTED }
    val invalidEntries: List<ImportCandidate> get() = candidates.filter { it.status == CandidateStatus.INVALID }
}

data class ImportResult(
    val importedCount: Int,
    val skippedDuplicatesCount: Int,
    val skippedUnsupportedCount: Int,
    val mode: ImportMode,
)

class ChtImportManager(
    private val cheatDao: CheatDao,
) {
    companion object {
        private val MASTER_CODE_REGEX =
            Regex("""\b(Master\s*Code|\(M\)|\[M\]|Enable\s*Code)\b""", RegexOption.IGNORE_CASE)

        fun normalizeCode(code: String): String {
            return code.replace(Regex("[^0-9A-Za-z]"), "").uppercase()
        }
    }

    fun generatePreview(
        inputStream: InputStream,
        fileName: String,
        game: Game,
        existingCheats: List<Cheat>,
    ): ImportPreview {
        val chtFile = ChtParser.parse(inputStream)
        return generatePreview(chtFile, fileName, game, existingCheats)
    }

    fun generatePreview(
        chtFile: ChtFile,
        fileName: String,
        game: Game,
        existingCheats: List<Cheat>,
    ): ImportPreview {
        val isSystemSupported = CheatSupport.isSystemSupported(game.systemId)
        val existingNormalizedCodes = existingCheats.map { normalizeCode(it.code) }.toSet()
        val seenInFileNormalizedCodes = mutableSetOf<String>()

        val candidates =
            chtFile.entries.map { entry ->
                val title = entry.description.trim().ifEmpty { "Cheat ${entry.index}" }
                val formattedCode = entry.codeWithNewlines
                val normalized = normalizeCode(formattedCode)
                val isMaster = MASTER_CODE_REGEX.containsMatchIn(title)

                if (!isSystemSupported) {
                    return@map ImportCandidate(
                        entry = entry,
                        title = title,
                        code = formattedCode,
                        detectedType = null,
                        enabled = entry.enabled,
                        isMasterCode = isMaster,
                        status = CandidateStatus.UNSUPPORTED,
                        reason = "System '${game.systemId}' is not supported for cheats",
                    )
                }

                if (formattedCode.isEmpty()) {
                    return@map ImportCandidate(
                        entry = entry,
                        title = title,
                        code = formattedCode,
                        detectedType = null,
                        enabled = entry.enabled,
                        isMasterCode = isMaster,
                        status = CandidateStatus.INVALID,
                        reason = "Cheat code is empty",
                    )
                }

                val detectedType = CheatValidator.detectType(game.systemId, formattedCode)
                if (detectedType == null) {
                    return@map ImportCandidate(
                        entry = entry,
                        title = title,
                        code = formattedCode,
                        detectedType = null,
                        enabled = entry.enabled,
                        isMasterCode = isMaster,
                        status = CandidateStatus.UNSUPPORTED,
                        reason = "Code format is not supported for ${game.systemId.uppercase()}",
                    )
                }

                val validation = CheatValidator.validate(detectedType, formattedCode)
                if (validation is ValidationResult.Invalid) {
                    return@map ImportCandidate(
                        entry = entry,
                        title = title,
                        code = formattedCode,
                        detectedType = detectedType,
                        enabled = entry.enabled,
                        isMasterCode = isMaster,
                        status = CandidateStatus.INVALID,
                        reason = validation.message,
                    )
                }

                if (existingNormalizedCodes.contains(normalized)) {
                    return@map ImportCandidate(
                        entry = entry,
                        title = title,
                        code = formattedCode,
                        detectedType = detectedType,
                        enabled = entry.enabled,
                        isMasterCode = isMaster,
                        status = CandidateStatus.DUPLICATE,
                        reason = "Already exists in current game cheats",
                    )
                }

                if (seenInFileNormalizedCodes.contains(normalized)) {
                    return@map ImportCandidate(
                        entry = entry,
                        title = title,
                        code = formattedCode,
                        detectedType = detectedType,
                        enabled = entry.enabled,
                        isMasterCode = isMaster,
                        status = CandidateStatus.DUPLICATE,
                        reason = "Duplicate entry within file",
                    )
                }

                seenInFileNormalizedCodes.add(normalized)

                ImportCandidate(
                    entry = entry,
                    title = title,
                    code = formattedCode,
                    detectedType = detectedType,
                    enabled = entry.enabled,
                    isMasterCode = isMaster,
                    status = CandidateStatus.VALID,
                )
            }

        return ImportPreview(
            fileName = fileName,
            systemId = game.systemId,
            totalEntries = chtFile.entries.size,
            candidates = candidates,
        )
    }

    suspend fun executeImport(
        game: Game,
        preview: ImportPreview,
        mode: ImportMode,
    ): ImportResult {
        val toImport =
            if (mode == ImportMode.MERGE) {
                preview.validEntries
            } else {
                val seen = mutableSetOf<String>()
                preview.candidates.filter { candidate ->
                    val isInvalidOrUnsupported =
                        candidate.status == CandidateStatus.UNSUPPORTED ||
                            candidate.status == CandidateStatus.INVALID
                    if (isInvalidOrUnsupported) {
                        false
                    } else {
                        val norm = normalizeCode(candidate.code)
                        if (seen.contains(norm)) {
                            false
                        } else {
                            seen.add(norm)
                            true
                        }
                    }
                }
            }

        val existingCheats = cheatDao.getCheatsForGameSync(game.id)
        val startOrderIndex = if (mode == ImportMode.MERGE) existingCheats.size else 0

        val cheatEntities =
            toImport.mapIndexed { idx, candidate ->
                Cheat(
                    gameId = game.id,
                    title = candidate.title,
                    systemId = game.systemId,
                    cheatType = candidate.detectedType?.id ?: "",
                    code = candidate.code,
                    enabled = candidate.enabled,
                    isMasterCode = candidate.isMasterCode,
                    orderIndex = startOrderIndex + idx,
                )
            }

        if (mode == ImportMode.REPLACE) {
            cheatDao.replaceCheatsForGame(game.id, cheatEntities)
        } else {
            cheatDao.insertCheats(cheatEntities)
        }

        val skippedDuplicates =
            preview.totalEntries - toImport.size - preview.unsupportedEntries.size - preview.invalidEntries.size

        return ImportResult(
            importedCount = cheatEntities.size,
            skippedDuplicatesCount = skippedDuplicates.coerceAtLeast(0),
            skippedUnsupportedCount = preview.unsupportedEntries.size + preview.invalidEntries.size,
            mode = mode,
        )
    }
}
