package com.swordfish.lemuroid.lib.library

import com.swordfish.lemuroid.lib.library.db.entity.Game

object CoverArtPolicy {
    private data class Ratios(
        val usa: Float,
        val japanese: Float = usa,
    )

    // Aspect ratios were taken from the libretro database.
    private val ratiosBySystem =
        mapOf(
            SystemID.ATARI2600 to Ratios(0.7350f),
            SystemID.ATARI7800 to Ratios(0.7150f),
            SystemID.DOS to Ratios(0.80f),
            SystemID.FBNEO to Ratios(0.7529f),
            SystemID.GB to Ratios(usa = 1.0000f, japanese = 0.7930f),
            SystemID.GBA to Ratios(usa = 1.0000f, japanese = 1.5924f),
            SystemID.GBC to Ratios(usa = 1.0000f, japanese = 0.7975f),
            SystemID.GENESIS to Ratios(0.7102f),
            SystemID.GG to Ratios(0.7111f),
            SystemID.LYNX to Ratios(1.1179f),
            SystemID.MAME2003PLUS to Ratios(0.7160f),
            SystemID.N64 to Ratios(usa = 1.4342f, japanese = 0.7148f),
            SystemID.NDS to Ratios(1.1130f),
            SystemID.NES to Ratios(0.7308f),
            SystemID.NGC to Ratios(0.9964f),
            SystemID.NGP to Ratios(0.8767f),
            SystemID.NINTENDO_3DS to Ratios(1.1363f),
            SystemID.PC_ENGINE to Ratios(1.0000f),
            SystemID.PSP to Ratios(0.5804f),
            SystemID.PSX to Ratios(1.0000f),
            SystemID.SEGACD to Ratios(usa = 0.5919f, japanese = 1.0079f),
            SystemID.SMS to Ratios(0.7058f),
            SystemID.SNES to Ratios(usa = 1.4342f, japanese = 0.5527f),
            SystemID.WS to Ratios(0.8037f),
            SystemID.WSC to Ratios(0.6900f),
        )

    // TODO This should be probably migrated to the Game class directly.
    private val tagsRegex = Regex("\\(([^()]*)\\)")

    private fun isJapanese(title: String): Boolean {
        return tagsRegex.findAll(title)
            .any { match ->
                match.groupValues[1].split(',').any { it.trim() == "Japan" }
            }
    }

    fun aspectRatio(game: Game): Float {
        val ratios = ratiosBySystem[GameSystem.findById(game.systemId).id] ?: return 1f
        return if (isJapanese(game.title)) ratios.japanese else ratios.usa
    }
}
