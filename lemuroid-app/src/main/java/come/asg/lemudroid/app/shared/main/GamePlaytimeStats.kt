package come.asg.lemudroid.app.shared.main

import android.content.Context

/** Persisted local aggregate playtime; keyed by stable library game ID. */
object GamePlaytimeStats {
    private const val PREFS = "game_playtime_stats"
    private fun key(gameId: Int, suffix: String) = "game_${gameId}_$suffix"

    fun record(context: Context, gameId: Int, durationMs: Long) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(key(gameId, "total_ms"), prefs.getLong(key(gameId, "total_ms"), 0L) + durationMs.coerceAtLeast(0L))
            .putInt(key(gameId, "sessions"), prefs.getInt(key(gameId, "sessions"), 0) + 1)
            .putLong(key(gameId, "last_played"), System.currentTimeMillis())
            .apply()
    }

    fun totalMs(context: Context, gameId: Int): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(key(gameId, "total_ms"), 0L)

    fun sessions(context: Context, gameId: Int): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(key(gameId, "sessions"), 0)

    fun lastPlayed(context: Context, gameId: Int): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(key(gameId, "last_played"), 0L)
}
