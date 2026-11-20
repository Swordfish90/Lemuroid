package com.swordfish.lemuroid.lib.cheats

object CheatFormatter {

    fun splitCodeLines(
        type: CheatType,
        rawCode: String,
    ): List<String> {
        val rawLines = rawCode.trim().split(Regex("[\\r\\n+]+")).map { it.trim() }.filter { it.isNotEmpty() }
        if (rawLines.isEmpty()) return emptyList()

        val result = mutableListOf<String>()
        for (line in rawLines) {
            when (type) {
                CheatType.GB_GAMESHARK -> {
                    val tokens = line.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                    if (tokens.size > 1 && tokens.all { it.replace("-", "").length == 8 }) {
                        result.addAll(tokens)
                    } else if (tokens.size == 2 && tokens[0].length == 4 && tokens[1].length == 4) {
                        result.add(tokens.joinToString(""))
                    } else {
                        result.add(line)
                    }
                }
                CheatType.GB_GAME_GENIE -> {
                    val tokens = line.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                    if (tokens.size > 1 && tokens.all { it.replace("-", "").length in listOf(6, 9) }) {
                        result.addAll(tokens)
                    } else {
                        result.add(line)
                    }
                }
                CheatType.SNES_GAME_GENIE -> {
                    val tokens = line.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                    if (tokens.size > 1 && tokens.all { it.replace("-", "").length == 8 }) {
                        result.addAll(tokens)
                    } else {
                        result.add(line)
                    }
                }
                CheatType.SNES_PRO_ACTION_REPLAY -> {
                    val tokens = line.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                    if (tokens.size > 1 && tokens.all { it.replace(":", "").length == 8 }) {
                        result.addAll(tokens)
                    } else {
                        result.add(line)
                    }
                }
                CheatType.NES_GAME_GENIE -> {
                    val tokens = line.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                    if (tokens.size > 1 && tokens.all { it.replace("-", "").length in listOf(6, 8) }) {
                        result.addAll(tokens)
                    } else {
                        result.add(line)
                    }
                }
                CheatType.NES_PRO_ACTION_REPLAY -> {
                    val tokens = line.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                    if (tokens.size > 1 && tokens.all { it.replace(":", "").length in listOf(6, 8) }) {
                        result.addAll(tokens)
                    } else {
                        result.add(line)
                    }
                }
                CheatType.GBA_ACTION_REPLAY_V3 -> {
                    val tokens = line.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                    if (tokens.size > 2 && tokens.size % 2 == 0 && tokens.all { it.replace("-", "").length == 8 }) {
                        result.addAll(tokens.chunked(2).map { "${it[0]} ${it[1]}" })
                    } else {
                        result.add(line)
                    }
                }
                CheatType.GBA_CODEBREAKER -> {
                    val tokens = line.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                    val isValidPairs =
                        tokens.size > 2 && tokens.size % 2 == 0 &&
                            tokens.chunked(2).all {
                                it[0].replace("-", "").length == 8 && it[1].replace("-", "").length == 4
                            }
                    if (isValidPairs) {
                        result.addAll(tokens.chunked(2).map { "${it[0]} ${it[1]}" })
                    } else {
                        result.add(line)
                    }
                }
                CheatType.SNES_RAW,
                CheatType.NES_RAW,
                -> {
                    result.add(line)
                }
            }
        }
        return result
    }

    /**
     * Returns individual formatted code lines ready for separate retro_cheat_set calls
     * Each line is a single cheat code that should be sent to the core with its own index
     */

    fun formatLinesForCore(
        systemId: String,
        cheatType: String,
        rawCode: String,
    ): List<String> {
        val type = CheatType.findById(cheatType) ?: return listOf(rawCode.trim().uppercase()).filter { it.isNotEmpty() }
        val lines = splitCodeLines(type, rawCode)
        if (lines.isEmpty()) return emptyList()

        return lines.map { formatSingleLine(type, it) }
    }

    fun formatForCore(
        systemId: String,
        cheatType: String,
        rawCode: String,
    ): String {
        return formatLinesForCore(systemId, cheatType, rawCode).joinToString("+")
    }

    private fun formatSingleLine(
        type: CheatType,
        line: String,
    ): String {
        return when (type) {
            CheatType.NES_GAME_GENIE -> {
                line.replace("-", "").replace(" ", "").uppercase()
            }
            CheatType.NES_PRO_ACTION_REPLAY,
            CheatType.NES_RAW,
            -> {
                line.replace(" ", "").uppercase()
            }
            CheatType.SNES_GAME_GENIE -> {
                val cleaned = line.replace(" ", "").replace("-", "").uppercase()
                if (cleaned.length == 8) {
                    "${cleaned.substring(0, 4)}-${cleaned.substring(4)}"
                } else {
                    cleaned
                }
            }
            CheatType.SNES_PRO_ACTION_REPLAY,
            CheatType.SNES_RAW,
            -> {
                line.replace(" ", "").uppercase()
            }
            CheatType.GB_GAME_GENIE -> {
                val cleaned = line.replace(" ", "").replace("-", "").uppercase()
                if (cleaned.length == 9) {
                    "${cleaned.substring(0, 3)}-${cleaned.substring(3, 6)}-${cleaned.substring(6)}"
                } else if (cleaned.length == 6) {
                    "${cleaned.substring(0, 3)}-${cleaned.substring(3)}"
                } else {
                    line.replace(" ", "").uppercase()
                }
            }
            CheatType.GB_GAMESHARK -> {
                val cleaned = line.replace(" ", "").replace("-", "").uppercase()
                // Game Boy Color GameShark v3 uses 90-97 prefixes for banked RAM writes
                // Gambatte core only implements 01 and ignores 9x codes so we will map 90-97 to 01 so the address is written
                if (cleaned.length == 8 && cleaned.matches(Regex("^9[0-7][0-9A-F]{6}$"))) {
                    "01" + cleaned.substring(2)
                } else {
                    cleaned
                }
            }
            CheatType.GBA_ACTION_REPLAY_V3 -> {
                val tokens = line.trim().split(Regex("\\s+"))
                if (tokens.size == 2) {
                    "${tokens[0].uppercase()} ${tokens[1].uppercase()}"
                } else {
                    val cleaned = line.replace(" ", "").replace("-", "").uppercase()
                    if (cleaned.length == 16) {
                        "${cleaned.substring(0, 8)} ${cleaned.substring(8)}"
                    } else {
                        cleaned
                    }
                }
            }
            CheatType.GBA_CODEBREAKER -> {
                val tokens = line.trim().split(Regex("\\s+"))
                if (tokens.size == 2) {
                    "${tokens[0].uppercase()} ${tokens[1].uppercase()}"
                } else {
                    val cleaned = line.replace(" ", "").replace("-", "").uppercase()
                    if (cleaned.length == 12) {
                        "${cleaned.substring(0, 8)} ${cleaned.substring(8)}"
                    } else {
                        cleaned
                    }
                }
            }
        }
    }
}
