package com.swordfish.lemuroid.app.tv.input

import android.os.Bundle
import android.view.KeyEvent
import androidx.leanback.app.GuidedStepSupportFragment
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.ShortcutBindingUpdater
import com.swordfish.lemuroid.app.tv.shared.BaseTVActivity
import javax.inject.Inject

class TVGamePadShortcutBindingActivity : BaseTVActivity() {
    @Inject
    lateinit var inputDeviceManager: InputDeviceManager

    private lateinit var shortcutBindingUpdater: ShortcutBindingUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shortcutBindingUpdater = ShortcutBindingUpdater(inputDeviceManager, intent)

        if (null == savedInstanceState) {
            val fragment =
                TVGamePadBindingFragment.create(
                    shortcutBindingUpdater.getTitle(applicationContext),
                    shortcutBindingUpdater.getMessage(applicationContext),
                )
            GuidedStepSupportFragment.addAsRoot(this, fragment, android.R.id.content)
        }
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent,
    ): Boolean {
        return shortcutBindingUpdater.handleKeyEvent(event)
    }

    override fun onKeyUp(
        keyCode: Int,
        event: KeyEvent,
    ): Boolean {
        val result = shortcutBindingUpdater.handleKeyEvent(event)

        if (result) {
            finish()
        }

        return result
    }
}
