package com.swordfish.lemuroid.app.feature.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.cache.CacheCleanerWork
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * Used as entry to point to [GameActivity], and both run in a separate process.
 * Takes care of loading everything (the game has to be ready in onCreate of GameActivity).
 * GameSaverWork is launched in this seperate process.
 */
class GameLauncherActivity : ImmersiveActivity() {

    @Inject lateinit var gameLoader: GameLoader
    @Inject lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        if (savedInstanceState == null) {
            val gameId = intent.getIntExtra("game_id", -1)
            val loadSave = intent.getBooleanExtra("load_save", false)

            val loadingStatesSubject = PublishSubject.create<GameLoader.LoadingState>()
            loadingStatesSubject.subscribeOn(Schedulers.io())
                    .throttleLast(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .autoDispose(scope())
                    .subscribe { displayLoadingState(it) }

            gameLoader.load(gameId, loadSave && settingsManager.autoSave)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .autoDispose(scope())
                    .subscribe(
                            {
                                loadingStatesSubject.onNext(it)
                                if (it is GameLoader.LoadingState.Ready) {
                                    onGameDataReady(it.gameData)
                                    loadingStatesSubject.onComplete()
                                }
                            },
                            {
                                Timber.e(it, "Error while loading game ${it.message}")
                                displayGenericErrorMessage()
                            }
                    )
        }
    }

    private fun onGameDataReady(gameData: GameLoader.GameData) {
        GameActivity.setTransientSaveRAMState(gameData.saveRAMData)
        GameActivity.setTransientQuickSave(gameData.quickSaveData)
        startActivityForResult(newIntent(this, gameData), REQUEST_CODE_GAME)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun displayLoadingState(loadingState: GameLoader.LoadingState) {
        val message = when (loadingState) {
            is GameLoader.LoadingState.LoadingCore -> getString(R.string.game_loading_download_core)
            is GameLoader.LoadingState.LoadingGame -> getString(R.string.game_loading_preparing_game)
            else -> ""
        }
        findViewById<TextView>(R.id.loading_text).text = message
    }

    private fun displayGenericErrorMessage() {
        AlertDialog.Builder(this)
                .setMessage(R.string.game_play_generic_error_message)
                .setPositiveButton(R.string.ok) { _, _ -> finish() }
                .show()
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
        triggerCacheCleanup()
        finish()
    }

    private fun triggerCacheCleanup() {
        CacheCleanerWork.enqueueUniqueWork(applicationContext)
    }

    override fun onBackPressed() {
        // TODO... This is a workaround for a possibly bad bug.
        // We are eating the back event. Killing the process while copying the file might result in a corrupted ROM that
        // doesn't load until a clear cache.
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
