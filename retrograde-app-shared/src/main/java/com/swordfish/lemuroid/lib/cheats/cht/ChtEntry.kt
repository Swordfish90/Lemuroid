package com.swordfish.lemuroid.lib.cheats.cht

data class ChtEntry(
    val index: Int,
    val description: String,
    val code: String,
    val enabled: Boolean = false,
    val handler: Int? = null,
    val memorySearchSize: Int? = null,
    val cheatType: Int? = null,
    val repeatCount: Int? = null,
    val repeatAddToValue: Int? = null,
    val repeatAddToAddress: Int? = null,
    val extraFields: Map<String, String> = emptyMap(),
) {
    val codeWithNewlines: String
        get() =
            code
                .split("+")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString("\n")
}
