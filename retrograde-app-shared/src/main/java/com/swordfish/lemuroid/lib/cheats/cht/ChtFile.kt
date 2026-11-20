package com.swordfish.lemuroid.lib.cheats.cht

data class ChtFile(
    val cheatCountHeader: Int? = null,
    val entries: List<ChtEntry> = emptyList(),
) {
    val size: Int get() = entries.size
    val isEmpty: Boolean get() = entries.isEmpty()
}
