package com.swordfish.lemuroid.app.tv.game

import android.os.Bundle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.tv.gamemenu.TVGameMenuActivity
import com.swordfish.lemuroid.common.displayToast
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import timber.log.Timber

class TVGameActivity : BaseGameActivity() {

    override fun getDialogClass() = TVGameMenuActivity::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inputDeviceManager
            .getEnabledInputsObservable()
            .filter { it.isEmpty() }
            .autoDispose(scope())
            .subscribeBy(Timber::e) { displayToast(R.string.tv_game_message_missing_gamepad) }
    }
}
