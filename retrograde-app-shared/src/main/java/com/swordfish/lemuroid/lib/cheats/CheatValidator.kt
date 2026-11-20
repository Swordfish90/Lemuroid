package com.swordfish.lemuroid.lib.cheats

import com.swordfish.lemuroid.lib.library.SystemID

sealed class ValidationResult {
    data object Valid : ValidationResult()

    data class Invalid(val message: String) : ValidationResult()
}

object CheatValidator {
    private val NES_GG_ALPHABET =
        setOf(
            'A', 'P', 'Z', 'L', 'G', 'I', 'T', 'Y',
            'E', 'O', 'X', 'U', 'K', 'S', 'V', 'N',
        )

    private val HEX_CHARS = "0123456789ABCDEFabcdef"

    /**
     * Detects the cheat type based on the code and system
     * Returns null if the cheat type cannot be determined
     */
    fun detectType(
        systemId: String,
        code: String,
    ): CheatType? {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return null
        val lines = trimmed.split(Regex("[\\r\\n+]+")).map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return null
        val firstLine = lines.first()

        return when (systemId.lowercase()) {
            SystemID.GB.dbname, SystemID.GBC.dbname -> {
                val tokens = firstLine.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                if (tokens.size > 1 &&
                    tokens.all {
                        it.replace("-", "").length == 8 &&
                            it.replace("-", "").all {
                                    c ->
                                c in HEX_CHARS
                            }
                    }
                ) {
                    CheatType.GB_GAMESHARK
                } else if (tokens.size > 1 &&
                    tokens.all {
                        it.replace("-", "").length in listOf(6, 9) &&
                            it.replace("-", "").all {
                                    c ->
                                c in HEX_CHARS
                            }
                    }
                ) {
                    CheatType.GB_GAME_GENIE
                } else {
                    val cleaned = firstLine.replace(" ", "").replace("-", "")
                    val isGgLen = cleaned.length == 6 || cleaned.length == 9
                    if (firstLine.contains("-") || isGgLen) {
                        CheatType.GB_GAME_GENIE
                    } else if (cleaned.length == 8 && cleaned.all { it in HEX_CHARS }) {
                        CheatType.GB_GAMESHARK
                    } else {
                        null
                    }
                }
            }
            SystemID.GBA.dbname -> {
                val tokens = firstLine.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                val cleaned = firstLine.replace(" ", "").replace("-", "")
                val isArTokens = tokens.size == 2 && tokens[0].length == 8 && tokens[1].length == 8
                val isCbTokens = tokens.size == 2 && tokens[0].length == 8 && tokens[1].length == 4
                val isCbChunked =
                    cleaned.length % 12 == 0 && tokens.size % 2 == 0 &&
                        tokens.chunked(2).all {
                            it[0].replace("-", "").length == 8 && it[1].replace("-", "").length == 4
                        }

                if (cleaned.length % 16 == 0 && tokens.all { it.replace("-", "").length == 8 }) {
                    CheatType.GBA_ACTION_REPLAY_V3
                } else if (isCbChunked) {
                    CheatType.GBA_CODEBREAKER
                } else if (cleaned.length == 16 || isArTokens) {
                    CheatType.GBA_ACTION_REPLAY_V3
                } else if (cleaned.length == 12 || isCbTokens) {
                    CheatType.GBA_CODEBREAKER
                } else {
                    null
                }
            }
            SystemID.SNES.dbname -> {
                val cleaned = firstLine.replace(" ", "")
                val isParPrefix = cleaned.uppercase().startsWith("7E") || cleaned.uppercase().startsWith("7F")
                if (cleaned.contains("=")) {
                    CheatType.SNES_RAW
                } else if (firstLine.contains("-")) {
                    CheatType.SNES_GAME_GENIE
                } else if (isParPrefix || cleaned.contains(":")) {
                    CheatType.SNES_PRO_ACTION_REPLAY
                } else if (cleaned.length == 8) {
                    CheatType.SNES_GAME_GENIE
                } else {
                    null
                }
            }
            SystemID.NES.dbname -> {
                val cleaned = firstLine.replace(" ", "").replace("-", "")
                val isGgLen = cleaned.length == 6 || cleaned.length == 8
                val isGgAlpha = isGgLen && cleaned.uppercase().all { it in NES_GG_ALPHABET }
                if (cleaned.contains("=") || cleaned.count { it == ':' } >= 2) {
                    CheatType.NES_RAW
                } else if (firstLine.contains(":") || cleaned.any { it.isDigit() }) {
                    CheatType.NES_PRO_ACTION_REPLAY
                } else if (isGgAlpha) {
                    CheatType.NES_GAME_GENIE
                } else {
                    null
                }
            }
            else -> null
        }
    }

    fun validate(
        type: CheatType,
        code: String,
    ): ValidationResult {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Cheat code cannot be empty")
        }

        val lines = CheatFormatter.splitCodeLines(type, trimmed)
        if (lines.isEmpty()) {
            return ValidationResult.Invalid("Cheat code cannot be empty")
        }

        if (!type.isMultiLineSupported && lines.size > 1) {
            return ValidationResult.Invalid("${type.displayName} does not support multiple lines")
        }

        for ((index, line) in lines.withIndex()) {
            val lineNumber = index + 1
            val result = validateSingleLine(type, line)
            if (result is ValidationResult.Invalid) {
                return if (lines.size > 1) {
                    ValidationResult.Invalid("Line $lineNumber: ${result.message}")
                } else {
                    result
                }
            }
        }

        return ValidationResult.Valid
    }

    private fun validateSingleLine(
        type: CheatType,
        line: String,
    ): ValidationResult {
        return when (type) {
            CheatType.NES_GAME_GENIE -> validateNesGameGenie(line)
            CheatType.NES_PRO_ACTION_REPLAY -> validateNesPar(line)
            CheatType.NES_RAW -> validateNesRaw(line)
            CheatType.SNES_GAME_GENIE -> validateSnesGameGenie(line)
            CheatType.SNES_PRO_ACTION_REPLAY -> validateSnesPar(line)
            CheatType.SNES_RAW -> validateSnesRaw(line)
            CheatType.GB_GAME_GENIE -> validateGbGameGenie(line)
            CheatType.GB_GAMESHARK -> validateGbGameShark(line)
            CheatType.GBA_ACTION_REPLAY_V3 -> validateGbaActionReplay(line)
            CheatType.GBA_CODEBREAKER -> validateGbaCodeBreaker(line)
        }
    }

    private fun validateNesGameGenie(line: String): ValidationResult {
        val cleaned = line.replace("-", "").replace(" ", "").uppercase()
        if (cleaned.length != 6 && cleaned.length != 8) {
            return ValidationResult.Invalid("NES Game Genie code must be 6 or 8 letters (got ${cleaned.length})")
        }
        for (char in cleaned) {
            if (char !in NES_GG_ALPHABET) {
                return ValidationResult.Invalid(
                    "Invalid NES Game Genie character '$char'. Allowed: A, P, Z, L, G, I, T, Y, E, O, X, U, K, S, V, N",
                )
            }
        }
        return ValidationResult.Valid
    }

    private fun validateNesPar(line: String): ValidationResult {
        val cleaned = line.replace(" ", "")
        if (cleaned.contains(":")) {
            val parts = cleaned.split(":")
            if (parts.size == 2 && parts[0].length == 4 && parts[1].length == 2 &&
                parts[0].all { it in HEX_CHARS } && parts[1].all { it in HEX_CHARS }
            ) {
                return ValidationResult.Valid
            }
            return ValidationResult.Invalid("Invalid NES PAR format. Expected XXXX:YY")
        }
        if (cleaned.length == 8 && cleaned.all { it in HEX_CHARS }) {
            return ValidationResult.Valid
        }
        return ValidationResult.Invalid("NES Pro Action Replay code must be 8 hex digits (e.g. 00XXXXYY or XXXX:YY)")
    }

    private fun validateNesRaw(line: String): ValidationResult {
        val cleaned = line.replace(" ", "")
        val parts = cleaned.split(":")
        if ((parts.size == 2 || parts.size == 3) &&
            parts[0].length == 4 && parts[0].all { it in HEX_CHARS } &&
            parts[1].length == 2 && parts[1].all { it in HEX_CHARS } &&
            (parts.size == 2 || (parts[2].length == 2 && parts[2].all { it in HEX_CHARS }))
        ) {
            return ValidationResult.Valid
        }
        return ValidationResult.Invalid("NES Raw code must be AAAA:VV or AAAA:VV:CC")
    }

    private fun validateSnesGameGenie(line: String): ValidationResult {
        val cleaned = line.replace(" ", "")
        val hyphenMatches = Regex("^[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}$").matches(cleaned)
        if (hyphenMatches) {
            return ValidationResult.Valid
        }
        val noHyphen = cleaned.replace("-", "")
        if (noHyphen.length == 8 && noHyphen.all { it in HEX_CHARS }) {
            return ValidationResult.Valid
        }
        return ValidationResult.Invalid("SNES Game Genie code must be 8 hex characters in XXXX-XXXX format")
    }

    private fun validateSnesPar(line: String): ValidationResult {
        val cleaned = line.replace(" ", "")
        if (cleaned.contains(":")) {
            val parts = cleaned.split(":")
            if (parts.size == 2 && parts[0].length == 6 && parts[1].length == 2 &&
                parts[0].all { it in HEX_CHARS } && parts[1].all { it in HEX_CHARS }
            ) {
                return ValidationResult.Valid
            }
            return ValidationResult.Invalid("Invalid SNES PAR format. Expected XXXXXX:YY")
        }
        if (cleaned.length == 8 && cleaned.all { it in HEX_CHARS }) {
            return ValidationResult.Valid
        }
        return ValidationResult.Invalid("SNES Pro Action Replay code must be 8 hex digits (e.g. XXXXXXYY)")
    }

    private fun validateSnesRaw(line: String): ValidationResult {
        val cleaned = line.replace(" ", "")
        val rawPattern = Regex("^[0-9A-Fa-f]{6}=[0-9A-Fa-f]{2}(\\?[0-9A-Fa-f]{2})?$")
        if (rawPattern.matches(cleaned)) {
            return ValidationResult.Valid
        }
        return ValidationResult.Invalid("SNES Raw code must be in address=byte format (e.g. 7E0010=99)")
    }

    private fun validateGbGameGenie(line: String): ValidationResult {
        val cleaned = line.replace(" ", "")
        val pattern6 = Regex("^[0-9A-Fa-f]{3}-[0-9A-Fa-f]{3}$")
        val pattern9 = Regex("^[0-9A-Fa-f]{3}-[0-9A-Fa-f]{3}-[0-9A-Fa-f]{3}$")
        if (pattern6.matches(cleaned) || pattern9.matches(cleaned)) {
            return ValidationResult.Valid
        }
        val noHyphen = cleaned.replace("-", "")
        if ((noHyphen.length == 6 || noHyphen.length == 9) && noHyphen.all { it in HEX_CHARS }) {
            return ValidationResult.Valid
        }
        return ValidationResult.Invalid("Game Boy Game Genie code must be XXX-XXX or XXX-XXX-XXX")
    }

    private fun validateGbGameShark(line: String): ValidationResult {
        val cleaned = line.replace(" ", "").replace("-", "")
        if (cleaned.length == 8 && cleaned.all { it in HEX_CHARS }) {
            return ValidationResult.Valid
        }
        return ValidationResult.Invalid("Game Boy GameShark code must be 8 hex characters (e.g. 01XXYYZZ)")
    }

    private fun validateGbaActionReplay(line: String): ValidationResult {
        val tokens = line.trim().split(Regex("\\s+"))
        if (tokens.size == 2 &&
            tokens[0].length == 8 && tokens[0].all { it in HEX_CHARS } &&
            tokens[1].length == 8 && tokens[1].all { it in HEX_CHARS }
        ) {
            return ValidationResult.Valid
        }
        val cleaned = line.replace(" ", "").replace("-", "")
        if (cleaned.length == 16 && cleaned.all { it in HEX_CHARS }) {
            return ValidationResult.Valid
        }
        return ValidationResult.Invalid("GBA Action Replay code must be 16 hex characters (XXXXXXXX YYYYYYYY)")
    }

    private fun validateGbaCodeBreaker(line: String): ValidationResult {
        val tokens = line.trim().split(Regex("\\s+"))
        if (tokens.size == 2 &&
            tokens[0].length == 8 && tokens[0].all { it in HEX_CHARS } &&
            tokens[1].length == 4 && tokens[1].all { it in HEX_CHARS }
        ) {
            return ValidationResult.Valid
        }
        val cleaned = line.replace(" ", "").replace("-", "")
        if (cleaned.length == 12 && cleaned.all { it in HEX_CHARS }) {
            return ValidationResult.Valid
        }
        return ValidationResult.Invalid("GBA CodeBreaker code must be 12 hex characters (XXXXXXXX YYYY)")
    }
}
