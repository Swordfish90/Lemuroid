package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.app.shared.library.LibraryIndexMonitor
import com.swordfish.lemuroid.app.tv.shared.TVHelper

/**
 * Used as entry to point to [BaseGameActivity], and both run in a separate process.
 * Takes care of loading everything (the game has to be ready in onCreate of BaseGameActivity).
 * GameSaverWork is launched in this seperate process.
 */
class ExternalGameLauncherActivity : ImmersiveActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_loading)
        if (savedInstanceState == null) {

            val gameId = intent.data?.pathSegments?.let { it[it.size - 1].toInt() }!!

            val liveData = LibraryIndexMonitor(applicationContext).getLiveData()
            liveData.observe(this) {
                if (!it) {
                    GameLauncherActivity.launchGame(this, gameId, true, TVHelper.isTV(applicationContext))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    liveData.removeObservers(this)
                }
            }
        }
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
}
