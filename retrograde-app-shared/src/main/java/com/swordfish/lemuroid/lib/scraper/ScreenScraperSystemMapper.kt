package com.swordfish.lemuroid.lib.scraper

import com.swordfish.lemuroid.lib.library.SystemID

// Maps Lemuroid SystemID to ScreenScraper's numeric systemeid.

object ScreenScraperSystemMapper {
    fun getSystemEid(systemID: SystemID): Int? {
        return when (systemID) {
            SystemID.NES -> 3
            SystemID.SNES -> 4
            SystemID.GENESIS -> 1
            SystemID.GB -> 9
            SystemID.GBC -> 10
            SystemID.GBA -> 12
            SystemID.N64 -> 14
            SystemID.SMS -> 2
            SystemID.PSP -> 61
            SystemID.NDS -> 15
            SystemID.GG -> 21
            SystemID.ATARI2600 -> 26
            SystemID.PSX -> 57
            SystemID.FBNEO -> 75
            SystemID.MAME2003PLUS -> 75
            SystemID.PC_ENGINE -> 31
            SystemID.LYNX -> 28
            SystemID.ATARI7800 -> 40
            SystemID.SEGACD -> 20
            SystemID.NGP -> 82
            SystemID.NGC -> 13
            SystemID.WS -> 45
            SystemID.WSC -> 46
            SystemID.DOS -> 135
            SystemID.NINTENDO_3DS -> 17
        }
    }

    fun getSystemEid(systemIdString: String): Int? {
        val systemID = runCatching {
            SystemID.values().firstOrNull { it.dbname.equals(systemIdString, ignoreCase = true) || it.name.equals(systemIdString, ignoreCase = true) }
        }.getOrNull()
        return systemID?.let { getSystemEid(it) }
    }
}
