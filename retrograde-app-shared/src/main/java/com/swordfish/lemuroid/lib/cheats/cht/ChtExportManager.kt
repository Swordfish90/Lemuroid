package com.swordfish.lemuroid.lib.cheats.cht

import com.swordfish.lemuroid.lib.library.db.entity.Cheat
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.io.OutputStream

class ChtExportManager {
    fun export(
        outputStream: OutputStream,
        cheats: List<Cheat>,
    ) {
        ChtSerializer.serialize(outputStream, cheats)
    }

    companion object {
        fun buildSafeExportFileName(game: Game): String {
            val safeTitle =
                game.title
                    .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                    .trim()
                    .ifEmpty { "game" }
            return "$safeTitle.cht"
        }
    }
}
