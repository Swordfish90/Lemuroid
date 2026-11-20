package com.swordfish.lemuroid.lib.cheats

import com.swordfish.lemuroid.lib.library.SystemID

object CheatSupport {
    private val SUPPORTED_SYSTEMS =
        setOf(
            SystemID.NES.dbname,
            SystemID.SNES.dbname,
            SystemID.GB.dbname,
            SystemID.GBC.dbname,
            SystemID.GBA.dbname,
        )

    fun isSystemSupported(systemId: String): Boolean {
        return SUPPORTED_SYSTEMS.contains(systemId)
    }

    fun getSupportedSystems(): Set<String> = SUPPORTED_SYSTEMS
}
