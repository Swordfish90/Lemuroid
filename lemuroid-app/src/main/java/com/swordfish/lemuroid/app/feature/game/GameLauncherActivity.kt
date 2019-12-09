package com.swordfish.lemuroid.app.feature.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.work.WorkManager
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.android.RetrogradeActivity
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.game.GameSaveWorker
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * Used as entry to point to [GameActivity], and both run in a separate process.
 * Takes care of loading everything (the game has to be ready in onCreate of GameActivity).
 * GameSaverWork is launched in this seperate process.
 */
class GameLauncherActivity : RetrogradeActivity() {

    @Inject lateinit var gameLoader: GameLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        if (savedInstanceState == null) {
            val gameId = intent.getIntExtra("game_id", -1)
            val loadSave = intent.getBooleanExtra("load_save", false)
            gameLoader.load(gameId, loadSave)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .autoDispose(scope())
                    .subscribe(
                            {
                                GameActivity.setTransientSaveState(it.saveData)
                                startActivityForResult(newIntent(this, it), REQUEST_CODE_GAME)
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            },
                            { Timber.e(it, "Error while loading game ${it.message}") }
                    )
        }
    }

    fun newIntent(context: Context, gameData: GameLoader.GameData) =
            Intent(context, GameActivity::class.java).apply {
                putExtra(GameActivity.EXTRA_SYSTEM_ID, gameData.game.systemId)
                putExtra(GameActivity.EXTRA_GAME_ID, gameData.game.id)
                putExtra(GameActivity.EXTRA_CORE_PATH, gameData.coreFile.absolutePath)
                putExtra(GameActivity.EXTRA_GAME_PATH, gameData.gameFile.absolutePath)
            }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GAME && resultCode == RESULT_OK && data != null) {
            val gameId = data.getIntExtra(GameActivity.EXTRA_GAME_ID, -1)
            val saveFile = data.getStringExtra(GameActivity.EXTRA_SAVE_FILE)
            if (gameId != -1 && saveFile != null) {
                WorkManager.getInstance(applicationContext).enqueue(GameSaveWorker.newRequest(gameId, saveFile))
            }
        }
        setResult(resultCode)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        exitProcess(0)
    }

    companion object {
        private const val REQUEST_CODE_GAME = 1000

        fun launchGame(context: Context, game: Game, loadSave: Boolean) {
            context.startActivity(
                    Intent(context, GameLauncherActivity::class.java).apply {
                        putExtra("game_id", game.id)
                        putExtra("load_save", loadSave)
                    }
            )
        }
    }
}
