package come.asg.ominoy.app.mobile.feature.cheats

import come.asg.ominoy.lib.library.SystemID

/** Cheat formats offered for the supported Game Boy families. The persisted value is metadata only. */
enum class CheatCodeType(val persistedValue: String) {
    GAME_SHARK("game_shark"),
    GAME_GENIE("game_genie"),
    ACTION_REPLAY("action_replay"),
    CODE_BREAKER("code_breaker"),
    ;

    companion object {
        fun fromPersistedValue(value: String?): CheatCodeType? = values().firstOrNull { it.persistedValue == value }

        fun forSystem(systemId: String): List<CheatCodeType> =
            when (systemId) {
                SystemID.GB.dbname, SystemID.GBC.dbname -> listOf(GAME_SHARK, GAME_GENIE)
                SystemID.GBA.dbname -> listOf(GAME_SHARK, ACTION_REPLAY, CODE_BREAKER)
                else -> emptyList()
            }
    }
}

/** Validates user-entered cheat text and returns its canonical, uppercase core-code form. */
object CheatCodeValidator {
    const val MAX_NON_EMPTY_LINES = 8
    const val MAX_TRIMMED_LINE_LENGTH = 24

    private val hex = Regex("[0-9A-Fa-f]+")
    private val separators = Regex("[\\s,+]+")

    fun toEditableText(
        canonicalCode: String,
        type: CheatCodeType,
        systemId: String,
    ): String {
        val groups = canonicalCode.split(separators).filter(String::isNotBlank)
        val pairedGbaFormat =
            systemId == SystemID.GBA.dbname &&
                (
                    type == CheatCodeType.GAME_SHARK ||
                        type == CheatCodeType.ACTION_REPLAY ||
                        type == CheatCodeType.CODE_BREAKER
                )
        val groupsPerLine = if (pairedGbaFormat) 2 else 1
        return groups.chunked(groupsPerLine).joinToString("\n") { it.joinToString(" ") }
    }

    fun normalizeStored(
        canonicalCode: String,
        type: CheatCodeType,
        systemId: String,
    ): String? = normalize(toEditableText(canonicalCode, type, systemId), type, systemId)

    fun normalize(
        input: String,
        type: CheatCodeType,
        systemId: String,
    ): String? {
        val lines = input.lineSequence().map(String::trim).filter(String::isNotEmpty).toList()
        if (lines.isEmpty() || lines.size > MAX_NON_EMPTY_LINES) return null

        val normalizedLines =
            lines.map { line ->
                if (line.length > MAX_TRIMMED_LINE_LENGTH) return null
                normalizeLine(line, type, systemId) ?: return null
            }
        return normalizedLines.joinToString("+")
    }

    private fun normalizeLine(
        line: String,
        type: CheatCodeType,
        systemId: String,
    ): String? {
        return when (type) {
            CheatCodeType.GAME_SHARK -> {
                val groups = splitGroups(line) ?: return null
                if (systemId == SystemID.GBA.dbname) {
                    normalizePair(groups, setOf(8), allowCompactLength = 16)
                } else {
                    val codeGroups =
                        when {
                            groups.all { it.length == 8 } -> groups
                            groups.size == 1 && groups.single().length in setOf(16, 24) ->
                                groups.single().chunked(8)
                            else -> return null
                        }
                    codeGroups.joinToString("+") { it.uppercase() }
                }
            }
            CheatCodeType.GAME_GENIE -> {
                val groups = splitGroups(line, removeDashes = true) ?: return null
                if (groups.any { it.length !in setOf(6, 9) }) return null
                // mGBA's Game Boy parser requires Game Genie lines in XXX-XXX[-XXX] form.
                groups.joinToString("+") { group ->
                    group.uppercase().chunked(3).joinToString("-")
                }
            }
            CheatCodeType.ACTION_REPLAY -> {
                if (systemId != SystemID.GBA.dbname) return null
                val groups = splitGroups(line) ?: return null
                normalizePair(groups, setOf(8), allowCompactLength = 16)
            }
            CheatCodeType.CODE_BREAKER -> {
                if (systemId != SystemID.GBA.dbname) return null
                val groups = splitGroups(line) ?: return null
                normalizePair(groups, setOf(4), allowCompactLength = 12)
            }
        }
    }

    private fun normalizePair(
        groups: List<String>,
        secondGroupLengths: Set<Int>,
        allowCompactLength: Int,
    ): String? {
        val pair =
            when {
                groups.size == 2 -> groups
                groups.size == 1 && groups.single().length == allowCompactLength -> {
                    val compact = groups.single()
                    listOf(compact.substring(0, 8), compact.substring(8))
                }
                else -> return null
            }
        if (pair[0].length != 8 || pair[1].length !in secondGroupLengths) return null
        return pair.joinToString("+") { it.uppercase() }
    }

    private fun splitGroups(
        line: String,
        removeDashes: Boolean = false,
    ): List<String>? {
        val prepared = if (removeDashes) line.replace("-", "") else line
        val groups = prepared.split(separators).filter(String::isNotEmpty)
        if (groups.isEmpty() || groups.any { !hex.matches(it) }) return null
        return groups
    }
}
