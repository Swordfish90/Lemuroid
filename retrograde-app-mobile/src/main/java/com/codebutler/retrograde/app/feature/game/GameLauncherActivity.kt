package com.codebutler.retrograde.app.feature.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.work.WorkManager
import com.codebutler.retrograde.lib.android.RetrogradeActivity
import com.codebutler.retrograde.lib.game.GameLoader
import com.codebutler.retrograde.lib.game.GameSaveWorker
import com.codebutler.retrograde.lib.library.db.entity.Game
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Used as entry to point to [GameActivity], which runs in a separate process.
 * Ensures that [GameSaveWorker] runs in main process only.
 */
class GameLauncherActivity : RetrogradeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val gameIntent = Intent(this, GameActivity::class.java)
            gameIntent.putExtras(intent.extras!!)
            startActivityForResult(gameIntent, REQUEST_CODE_GAME)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GAME && resultCode == RESULT_OK && data != null) {
            val gameId = data.getIntExtra(GameActivity.EXTRA_GAME_ID, -1)
            val saveFile = data.getStringExtra(GameActivity.EXTRA_SAVE_FILE)
            if (gameId != -1 && saveFile != null) {
                WorkManager.getInstance(this).enqueue(GameSaveWorker.newRequest(gameId, saveFile))
            }
        }
        setResult(resultCode)
        finish()
    }

    companion object {
        private const val REQUEST_CODE_GAME = 1000

        fun launchGame(context: Context, gameLoader: GameLoader, game: Game) {
            // TODO FILIPPO... Provide graphical feedback that something is loading.
            gameLoader.load(game.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { context.startActivity(newIntent(context, it)) },
                        { Timber.e(it, "Error while loading game ${it.message}") }
                )
        }

        fun newIntent(context: Context, gameData: GameLoader.GameData) =
                Intent(context, GameLauncherActivity::class.java).apply {
                    putExtra(GameActivity.EXTRA_SYSTEM_ID, gameData.game.systemId)
                    putExtra(GameActivity.EXTRA_GAME_ID, gameData.game.id)
                    putExtra(GameActivity.EXTRA_CORE_PATH, gameData.coreFile.absolutePath)
                    putExtra(GameActivity.EXTRA_GAME_PATH, gameData.gameFile.absolutePath)
                }
    }
}
