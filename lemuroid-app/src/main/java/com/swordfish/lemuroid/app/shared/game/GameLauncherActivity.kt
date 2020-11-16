package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.game.GameActivity
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.app.tv.game.TVGameActivity
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.game.GameLoader
import com.swordfish.lemuroid.lib.game.GameLoaderException
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.SavesCoherencyEngine
import com.swordfish.lemuroid.lib.storage.cache.CacheCleanerWork
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * Used as entry to point to [BaseGameActivity], and both run in a separate process.
 * Takes care of loading everything (the game has to be ready in onCreate of BaseGameActivity).
 * GameSaverWork is launched in this seperate process.
 */
class GameLauncherActivity : ImmersiveActivity() {

    @Inject lateinit var gameLoader: GameLoader
    @Inject lateinit var settingsManager: SettingsManager
    @Inject lateinit var coresSelection: CoresSelection
    @Inject lateinit var savesCoherencyEngine: SavesCoherencyEngine

    var startGameTime: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startGameTime = System.currentTimeMillis()

        setContentView(R.layout.activity_loading)
        if (savedInstanceState == null) {
            val game = intent.getSerializableExtra(EXTRA_GAME) as Game
            val requestLoadSave = intent.getBooleanExtra(EXTRA_LOAD_SAVE, false)
            val useLeanback = intent.getBooleanExtra(EXTRA_LEANBACK, false)

            val loadingStatesSubject = PublishSubject.create<GameLoader.LoadingState>()
            loadingStatesSubject.subscribeOn(Schedulers.io())
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope())
                .subscribe { displayLoadingState(it) }

            val core = getCoreForGame(game)
            val loadState = requestLoadSave &&
                settingsManager.autoSave &&
                core.statesSupported &&
                !savesCoherencyEngine.hasMoreRecentInGameFile(game, core.coreID)

            gameLoader.load(applicationContext, game, core, loadState)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope())
                .subscribe(
                    {
                        loadingStatesSubject.onNext(it)
                        if (it is GameLoader.LoadingState.Ready) {
                            onGameDataReady(it.gameData, useLeanback)
                            loadingStatesSubject.onComplete()
                        }
                    },
                    {
                        displayGameLoaderError((it as GameLoaderException).error)
                    }
                )
        }
    }

    private fun getCoreForGame(game: Game): SystemCoreConfig {
        val system = GameSystem.findById(game.systemId)
        return coresSelection.getCoreConfigForSystem(system)
    }

    private fun onGameDataReady(gameData: GameLoader.GameData, useLeanback: Boolean) {
        BaseGameActivity.setTransientSaveRAMState(gameData.saveRAMData)
        BaseGameActivity.setTransientQuickSave(gameData.quickSaveData)
        startActivityForResult(newIntent(this, gameData, useLeanback), REQUEST_CODE_GAME)
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

    fun newIntent(context: Context, gameData: GameLoader.GameData, useLeanback: Boolean) =
        Intent(context, getGameActivityClass(useLeanback)).apply {
            putExtra(BaseGameActivity.EXTRA_GAME, gameData.game)
            putExtra(BaseGameActivity.EXTRA_CORE_CONFIG, gameData.systemCoreConfig)
            putExtra(BaseGameActivity.EXTRA_CORE_PATH, gameData.coreLibrary)
            putExtra(BaseGameActivity.EXTRA_GAME_PATH, gameData.gameFile.absolutePath)
            putExtra(BaseGameActivity.EXTRA_CORE_VARIABLES, gameData.coreVariables)
            putExtra(BaseGameActivity.EXTRA_LOAD_SRAM, gameData.saveRAMData != null)
            putExtra(BaseGameActivity.EXTRA_LOAD_AUTOSAVE, gameData.quickSaveData != null)
        }

    private fun getGameActivityClass(useLeanback: Boolean) = if (useLeanback) {
        TVGameActivity::class.java
    } else {
        GameActivity::class.java
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val resultIntent = Intent().apply {
            putExtra(PLAY_GAME_RESULT_SESSION_DURATION, System.currentTimeMillis() - startGameTime)
        }

        setResult(Activity.RESULT_OK, resultIntent)
        triggerCacheCleanup()
        finish()
    }

    private fun triggerCacheCleanup() {
        CacheCleanerWork.enqueueCleanCacheLRU(applicationContext)
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

        private const val EXTRA_GAME = "GAME"
        private const val EXTRA_LOAD_SAVE = "LOAD_SAVE"
        private const val EXTRA_LEANBACK = "LEANBACK"

        const val REQUEST_PLAY_GAME = 1001
        const val PLAY_GAME_RESULT_SESSION_DURATION = "PLAY_GAME_RESULT_SESSION_DURATION"

        fun launchGame(activity: Activity, game: Game, loadSave: Boolean, useLeanback: Boolean) {
            activity.startActivityForResult(
                Intent(activity, GameLauncherActivity::class.java).apply {
                    putExtra(EXTRA_GAME, game)
                    putExtra(EXTRA_LOAD_SAVE, loadSave)
                    putExtra(EXTRA_LEANBACK, useLeanback)
                },
                REQUEST_PLAY_GAME
            )
        }
    }
}
