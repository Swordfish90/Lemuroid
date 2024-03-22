package com.swordfish.lemuroid.app.mobile.feature.input

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.foundation.focusable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.onGloballyPositioned
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.shared.input.InputBindingUpdater
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.lib.android.RetrogradeActivity
import timber.log.Timber
import javax.inject.Inject

class GamePadBindingActivity : RetrogradeActivity() {
    @Inject
    lateinit var inputDeviceManager: InputDeviceManager

    private lateinit var inputBindingUpdater: InputBindingUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inputBindingUpdater = InputBindingUpdater(inputDeviceManager, intent)

        setContent {
            AppTheme {
                val focusRequester = remember { FocusRequester() }

                AlertDialog(
                    modifier =
                        Modifier
                            .focusRequester(focusRequester)
                            .focusable()
                            .onKeyEvent { handleKeyEvent(it.nativeKeyEvent) }
                            .onGloballyPositioned { focusRequester.requestFocus() },
                    title = { Text(text = inputBindingUpdater.getTitle(applicationContext)) },
                    text = { Text(text = inputBindingUpdater.getMessage(applicationContext)) },
                    onDismissRequest = { finish() },
                    confirmButton = {},
                )
            }
        }
    }

    private fun handleKeyEvent(event: KeyEvent): Boolean {
        Timber.i("Received key event: $event")
        val result = inputBindingUpdater.handleKeyEvent(event)

        if (event.action == KeyEvent.ACTION_UP && result) {
            finish()
        }

        return result
    }

    @dagger.Module
    abstract class Module
}
