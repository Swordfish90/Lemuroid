package come.asg.ominoy.app.shared.game

import come.asg.ominoy.lib.library.db.entity.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** In-process progress shared between the WorkManager worker and the Home screen. */
object RetroAchievementsSyncProgress {
    data class GameProgress(
        val game: Game,
        val downloaded: Int = 0,
        val total: Int = 0,
        val state: String = "Waiting",
        val complete: Boolean = false,
        val failed: Boolean = false,
    ) {
        val fraction: Float get() = if (total <= 0) if (complete && !failed) 1f else 0f else (downloaded.toFloat() / total).coerceIn(0f, 1f)
    }

    data class Progress(
        val running: Boolean = false,
        val games: List<GameProgress> = emptyList(),
        val finishedMessage: String? = null,
    ) {
        val completeGames: Int get() = games.count { it.complete }
        val failedGames: List<GameProgress> get() = games.filter { it.failed }
        val fraction: Float get() = if (games.isEmpty()) 0f else completeGames.toFloat() / games.size
    }

    private val mutable = MutableStateFlow(Progress())
    val state = mutable.asStateFlow()

    fun begin(games: List<Game>) {
        mutable.value = Progress(running = true, games = games.map { GameProgress(it) })
    }

    fun prepareWorker(games: List<Game>) {
        mutable.update { current ->
            if (current.running && current.games.isNotEmpty()) current
            else Progress(running = true, games = games.map { GameProgress(it) })
        }
    }

    fun update(gameId: Int, transform: (GameProgress) -> GameProgress) {
        mutable.update { current ->
            current.copy(games = current.games.map { if (it.game.id == gameId) transform(it) else it })
        }
    }

    fun finish(message: String) {
        mutable.update { it.copy(running = false, finishedMessage = message) }
    }
}
