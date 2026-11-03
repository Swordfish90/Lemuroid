package come.asg.lemudroid.app.shared.game

/**
 * Converts common compact cheat-code forms into the libretro format.
 *
 * For example, a GBA CodeBreaker code entered as 3202864200FE becomes
 * 32028642+00FE before it is passed to the active core.
 */
object CheatCodeFormatter {
    private val separator = Regex("[\\s+,;:]+")
    private val hexadecimal = Regex("[0-9A-Fa-f]+")

    /**
     * Legacy persisted cheats have no format metadata, so retain this permissive core-code path
     * alongside the type-aware validator used for newly entered cheats. Dashes are accepted as
     * visual separators (including conventional Game Genie spelling) but are not sent to cores.
     */
    fun normalize(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        val dashedGameGenie = trimmed.replace("-", "")
        val raw =
            if (trimmed.contains('-') && hexadecimal.matches(dashedGameGenie) && dashedGameGenie.length in setOf(6, 9)) {
                dashedGameGenie
            } else {
                trimmed.replace("-", " ")
            }

        val parts = raw.split(separator).filter(String::isNotEmpty)
        val groups =
            if (parts.size > 1) {
                parts.flatMap { part ->
                    if (part.length > 8 && part.length !in setOf(9) && hexadecimal.matches(part)) {
                        splitCompact(
                            part,
                        )
                    } else {
                        listOf(part)
                    }
                }
            } else {
                val compact = parts.singleOrNull() ?: return null
                if (!hexadecimal.matches(compact)) return null
                if (compact.length in setOf(6, 9)) return compact.uppercase()
                if (compact.length < 8 || compact.length > 64) return null
                splitCompact(compact)
            }

        if (groups.isEmpty() || groups.any { it.length !in setOf(4, 6, 8, 9) || !hexadecimal.matches(it) }) {
            return null
        }
        return groups.joinToString("+") { it.uppercase() }
    }

    private fun splitCompact(compact: String): List<String> {
        val result = mutableListOf<String>()
        var index = 0
        result += compact.substring(index, 8)
        index += 8
        while (index < compact.length) {
            val remaining = compact.length - index
            val size = if (remaining >= 8) 8 else 4
            if (remaining < size) return emptyList()
            result += compact.substring(index, index + size)
            index += size
        }
        return result
    }
}
