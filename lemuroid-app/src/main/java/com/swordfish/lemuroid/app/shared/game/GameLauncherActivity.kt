package com.swordfish.lemuroid.app.shared.game

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.game.GameActivity
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.app.tv.game.TVGameActivity
import com.swordfish.lemuroid.lib.game.GameLoader
import kotlin.system.exitProcess

/**
 * Used as entry to point to [BaseGameActivity], and both run in a separate process.
 * Takes care of loading everything (the game has to be ready in onCreate of BaseGameActivity).
 * GameSaverWork is launched in this seperate process.
 */
class GameLauncherActivity : ImmersiveActivity() {

    var startGameTime: Long = System.currentTimeMillis()

    private fun retrieveGameId(): Int {
        return intent.getIntExtra(EXTRA_GAME, -1)
    }

    private fun stopBackgroundWork() {
        SaveSyncWork.cancelManualWork(applicationContext)
        SaveSyncWork.cancelAutoWork(applicationContext)

        if (TVHelper.isTV(applicationContext)) {
            ChannelUpdateWork.cancel(applicationContext)
        }
    }

    private fun restartBackgroundWork() {
        SaveSyncWork.enqueueAutoWork(applicationContext, 5)
        CacheCleanerWork.enqueueCleanCacheLRU(applicationContext)

        if (TVHelper.isTV(applicationContext)) {
            ChannelUpdateWork.enqueue(applicationContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RemoteWorkManager

        setContentView(R.layout.activity_loading)
        if (savedInstanceState == null) {

        }
    }

    private fun displayLoadingState(loadingState: GameLoader.LoadingState) {
        val message = when (loadingState) {
            is GameLoader.LoadingState.LoadingCore -> getString(R.string.game_loading_download_core)
            is GameLoader.LoadingState.LoadingGame -> getString(R.string.game_loading_preparing_game)
            else -> ""
        }
        findViewById<TextView>(R.id.loading_text).text = message
    }

    private fun getGameActivityClass(useLeanback: Boolean) = if (useLeanback) {
        TVGameActivity::class.java
    } else {
        GameActivity::class.java
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        finish()
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
    }
}
