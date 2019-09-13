package com.codebutler.retrograde.app.feature.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.work.WorkManager
import com.codebutler.retrograde.lib.android.RetrogradeActivity
import com.codebutler.retrograde.lib.game.GameSaveWorker
import com.codebutler.retrograde.lib.library.db.entity.Game

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
                WorkManager.getInstance().enqueue(GameSaveWorker.newRequest(gameId, saveFile))
            }
        }
        setResult(resultCode)
        finish()
    }

    companion object {
        private const val REQUEST_CODE_GAME = 1000

        fun launchGame(context: Context, game: Game) = context.startActivity(newIntent(context, game))

        fun newIntent(context: Context, game: Game) =
                Intent(context, GameLauncherActivity::class.java).apply {
                    putExtra(GameActivity.EXTRA_GAME_ID, game.id)
                }
    }
}
