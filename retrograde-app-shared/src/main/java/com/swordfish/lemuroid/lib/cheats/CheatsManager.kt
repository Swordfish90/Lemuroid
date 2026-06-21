package com.swordfish.lemuroid.lib.cheats

import android.content.SharedPreferences
import dagger.Lazy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CheatsManager(private val sharedPreferences: Lazy<SharedPreferences>) {

    fun saveGameCheats(gameId: Int, cheats: List<CheatEntry>) {
        val json = Json.encodeToString(cheats)
        sharedPreferences.get().edit().putString(prefKey(gameId), json).apply()
    }

    fun loadGameCheats(gameId: Int): List<CheatEntry> {
        val json = sharedPreferences.get().getString(prefKey(gameId), null) ?: return emptyList()
        return runCatching { Json.decodeFromString<List<CheatEntry>>(json) }.getOrElse { emptyList() }
    }

    private fun prefKey(gameId: Int) = "cheats_game_$gameId"
}
