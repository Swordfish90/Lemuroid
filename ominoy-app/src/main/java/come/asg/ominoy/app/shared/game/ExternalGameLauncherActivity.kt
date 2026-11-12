package come.asg.ominoy.app.shared.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import come.asg.ominoy.R
import come.asg.ominoy.app.shared.ImmersiveActivity
import come.asg.ominoy.app.shared.library.PendingOperationsMonitor
import come.asg.ominoy.app.shared.main.GameLaunchTaskHandler
import come.asg.ominoy.app.tv.channel.ChannelUpdateWork
import come.asg.ominoy.app.tv.shared.TVHelper
import come.asg.ominoy.app.utils.android.displayErrorDialog
import come.asg.ominoy.common.animationDuration
import come.asg.ominoy.common.coroutines.launchOnState
import come.asg.ominoy.common.coroutines.safeLaunch
import come.asg.ominoy.common.longAnimationDuration
import come.asg.ominoy.lib.core.CoresSelection
import come.asg.ominoy.lib.library.db.RetrogradeDatabase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This activity is used as an entry point when launching games from external shortcuts. This activity
 * still runs in the main process so it can peek into background job status and wait for them to
 * complete.
 */
@OptIn(FlowPreview::class)
class ExternalGameLauncherActivity : ImmersiveActivity() {
    @Inject
    lateinit var retrogradeDatabase: RetrogradeDatabase

    @Inject
    lateinit var gameLaunchTaskHandler: GameLaunchTaskHandler

    @Inject
    lateinit var coresSelection: CoresSelection

    @Inject
    lateinit var gameLauncher: GameLauncher

    private val loadingState = MutableStateFlow(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_loading)
        if (savedInstanceState == null) {
            val gameId = parseGameId(intent)
            if (gameId == null) {
                displayErrorMessage()
                return
            }

            lifecycleScope.launch {
                loadingState.value = true
                try {
                    loadGame(gameId)
                } catch (e: Throwable) {
                    displayErrorMessage()
                }
                loadingState.value = false
            }

            launchOnState(Lifecycle.State.RESUMED) {
                initializeLoadingFlow(loadingState)
            }
        }
    }

    private fun parseGameId(launchIntent: Intent): Int? {
        val uri = launchIntent.data ?: return null
        if (launchIntent.action != Intent.ACTION_VIEW || uri.scheme != "ominoy" || uri.host != packageName) return null
        val segments = uri.pathSegments
        if (segments.size != 3 || segments[0] != "play-game" || segments[1] != "id") return null
        return segments[2].toIntOrNull()?.takeIf { it > 0 }
    }

    private suspend fun initializeLoadingFlow(loadingSubject: MutableStateFlow<Boolean>) {
        loadingSubject
            .debounce(longAnimationDuration().toLong())
            .collect {
                findViewById<View>(R.id.progressBar).isVisible = it
            }
    }

    private suspend fun loadGame(gameId: Int) {
        waitPendingOperations()

        val game =
            retrogradeDatabase.gameDao().selectById(gameId)
                ?: throw IllegalArgumentException("Game not found: $gameId")

        delay(animationDuration().toLong())

        val gameLaunchSuccessful =
            gameLauncher.launchGameAsync(
                this,
                game,
                true,
                TVHelper.isTV(applicationContext),
            )

        if (!gameLaunchSuccessful) {
            finish()
        }
    }

    private suspend fun waitPendingOperations() {
        getLoadingLiveData()
            .filter { !it }
            .first()
    }

    private fun displayErrorMessage() {
        displayErrorDialog(R.string.game_loader_error_load_game, R.string.ok) { finish() }
    }

    private fun getLoadingLiveData(): Flow<Boolean> {
        return PendingOperationsMonitor(applicationContext).anyOperationInProgress()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            BaseGameActivity.REQUEST_PLAY_GAME -> {
                val isLeanback = data?.extras?.getBoolean(BaseGameActivity.PLAY_GAME_RESULT_LEANBACK) == true

                GlobalScope.safeLaunch {
                    if (isLeanback) {
                        ChannelUpdateWork.enqueue(applicationContext)
                    }
                    gameLaunchTaskHandler.handleGameFinish(false, this@ExternalGameLauncherActivity, resultCode, data)
                    finish()
                }
            }
        }
    }
}
