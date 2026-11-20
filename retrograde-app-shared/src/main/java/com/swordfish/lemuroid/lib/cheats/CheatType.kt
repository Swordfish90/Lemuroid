package com.swordfish.lemuroid.lib.cheats

import com.swordfish.lemuroid.lib.library.SystemID

enum class CheatType(
    val id: String,
    val displayName: String,
    val formatHint: String,
    val systemIds: Set<String>,
    val isMultiLineSupported: Boolean = true,
) {
    NES_GAME_GENIE(
        id = "NES_GAME_GENIE",
        displayName = "Game Genie",
        formatHint = "e.g. GZLAUT or YSAOPE (6 or 8 letters)",
        systemIds = setOf(SystemID.NES.dbname),
        isMultiLineSupported = true,
    ),
    NES_PRO_ACTION_REPLAY(
        id = "NES_PRO_ACTION_REPLAY",
        displayName = "Pro Action Replay",
        formatHint = "e.g. 00001099 or 0010:99",
        systemIds = setOf(SystemID.NES.dbname),
        isMultiLineSupported = true,
    ),
    NES_RAW(
        id = "NES_RAW",
        displayName = "Raw",
        formatHint = "e.g. 0010:99 or 0010:99:00",
        systemIds = setOf(SystemID.NES.dbname),
        isMultiLineSupported = true,
    ),

    SNES_GAME_GENIE(
        id = "SNES_GAME_GENIE",
        displayName = "Game Genie",
        formatHint = "e.g. DDF4-0915 (XXXX-XXXX)",
        systemIds = setOf(SystemID.SNES.dbname),
        isMultiLineSupported = true,
    ),
    SNES_PRO_ACTION_REPLAY(
        id = "SNES_PRO_ACTION_REPLAY",
        displayName = "Pro Action Replay",
        formatHint = "e.g. 7E001099 or 7E0010:99",
        systemIds = setOf(SystemID.SNES.dbname),
        isMultiLineSupported = true,
    ),
    SNES_RAW(
        id = "SNES_RAW",
        displayName = "Raw",
        formatHint = "e.g. 7E0010=99",
        systemIds = setOf(SystemID.SNES.dbname),
        isMultiLineSupported = true,
    ),

    GB_GAME_GENIE(
        id = "GB_GAME_GENIE",
        displayName = "Game Genie",
        formatHint = "e.g. 001-23A or 001-23A-456",
        systemIds = setOf(SystemID.GB.dbname, SystemID.GBC.dbname),
        isMultiLineSupported = true,
    ),
    GB_GAMESHARK(
        id = "GB_GAMESHARK",
        displayName = "GameShark",
        formatHint = "e.g. 01034AD1 (8 hex digits)",
        systemIds = setOf(SystemID.GB.dbname, SystemID.GBC.dbname),
        isMultiLineSupported = true,
    ),

    GBA_ACTION_REPLAY_V3(
        id = "GBA_ACTION_REPLAY_V3",
        displayName = "Action Replay / GameShark",
        formatHint = "e.g. 92660A01 9BEE5435 (8+8 hex digits)",
        systemIds = setOf(SystemID.GBA.dbname),
        isMultiLineSupported = true,
    ),
    GBA_CODEBREAKER(
        id = "GBA_CODEBREAKER",
        displayName = "CodeBreaker",
        formatHint = "e.g. 82003884 0001 (8+4 hex digits)",
        systemIds = setOf(SystemID.GBA.dbname),
        isMultiLineSupported = true,
    ),
    ;

    companion object {
        fun findById(id: String): CheatType? {
            return entries.firstOrNull { it.id == id }
        }

        fun getTypesForSystem(systemId: String): List<CheatType> {
            return entries.filter { it.systemIds.contains(systemId) }
        }

        fun defaultForSystem(systemId: String): CheatType {
            return getTypesForSystem(systemId).firstOrNull() ?: NES_GAME_GENIE
        }
    }
}
