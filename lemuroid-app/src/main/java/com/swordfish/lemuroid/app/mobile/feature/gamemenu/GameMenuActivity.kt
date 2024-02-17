@file:Suppress("UNUSED", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.coreoptions.GameMenuCoreOptionsScreen
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.coreoptions.GameMenuCoreOptionsViewModel
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.states.GameMenuStatesScreen
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.states.GameMenuStatesViewModel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.coreoptions.LemuroidCoreOption
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.lib.android.RetrogradeComponentActivity
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.saves.StatesManager
import com.swordfish.lemuroid.lib.saves.StatesPreviewManager
import java.security.InvalidParameterException
import javax.inject.Inject

class GameMenuActivity : RetrogradeComponentActivity() {

    @Inject
    lateinit var inputDeviceManager: InputDeviceManager

    @Inject
    lateinit var statesManager: StatesManager

    @Inject
    lateinit var statesPreviewManager: StatesPreviewManager

    data class GameMenuRequest(
        val coreOptions: List<LemuroidCoreOption>,
        val advancedCoreOptions: List<LemuroidCoreOption>,
        val game: Game,
        val coreConfig: SystemCoreConfig,
        val audioEnabled: Boolean,
        val fastForwardSupported: Boolean,
        val fastForwardEnabled: Boolean,
        val numDisks: Int,
        val currentDisk: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val extras = intent.extras

        val gameMenuRequest = GameMenuRequest(
            coreOptions = (extras?.getSerializable(GameMenuContract.EXTRA_CORE_OPTIONS) as Array<LemuroidCoreOption>?)
                ?.toList()
                ?: throw InvalidParameterException("Missing EXTRA_CORE_OPTIONS"),
            advancedCoreOptions = (extras?.getSerializable(GameMenuContract.EXTRA_ADVANCED_CORE_OPTIONS) as Array<LemuroidCoreOption>?)
                ?.toList()
                ?: throw InvalidParameterException("Missing EXTRA_ADVANCED_CORE_OPTIONS"),
            game = extras?.getSerializable(GameMenuContract.EXTRA_GAME) as Game?
                ?: throw InvalidParameterException("Missing EXTRA_GAME"),
            coreConfig = extras?.getSerializable(GameMenuContract.EXTRA_SYSTEM_CORE_CONFIG) as SystemCoreConfig?
                ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_CORE_CONFIG"),
            audioEnabled = extras?.getBoolean(GameMenuContract.EXTRA_AUDIO_ENABLED, false) ?: false,
            fastForwardSupported = extras?.getBoolean(GameMenuContract.EXTRA_FAST_FORWARD_SUPPORTED, false) ?: false,
            fastForwardEnabled = extras?.getBoolean(GameMenuContract.EXTRA_FAST_FORWARD, false) ?: false,
            numDisks = extras?.getInt(GameMenuContract.EXTRA_DISKS, 0) ?: 0,
            currentDisk = extras?.getInt(GameMenuContract.EXTRA_CURRENT_DISK, 0) ?: 0,
        )

        setContent {
            GameMenuScreen(gameMenuRequest)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GameMenuScreen(gameMenuRequest: GameMenuRequest) {
        AppTheme(themeSystemUi = false) {
            val navController = rememberNavController()
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry.value?.destination

            val currentRoute = currentDestination?.route
                ?.let { GameMenuRoute.findByRoute(it) }
                ?: GameMenuRoute.HOME

            SideMenu {
                TopAppBar(
                    title = { Text(stringResource(currentRoute.titleId)) },
                    windowInsets = WindowInsets(0.dp),
                    navigationIcon = {
                        AnimatedContent(targetState = currentRoute.canGoBack(), label = "Back") { canGoBack ->
                            if (canGoBack) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.Filled.ArrowBack,
                                        "Back"
                                    ) // TODO COMPOSE FIX CONTENT DESCRIPTION
                                }
                            } else {
                                IconButton(onClick = { onResult { } }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        "Close"
                                    ) // TODO COMPOSE FIX CONTENT DESCRIPTION
                                }
                            }
                        }
                    },
                )
                Divider(modifier = Modifier.fillMaxWidth())
                NavHost(
                    modifier = Modifier
                        .fillMaxSize(),
                    navController = navController,
                    startDestination = GameMenuRoute.HOME.route,
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() }
                ) {
                    composable(GameMenuRoute.HOME) {
                        GameMenuHomeScreen(navController, gameMenuRequest, ::onResult)
                    }
                    composable(GameMenuRoute.SAVE) {
                        GameMenuStatesScreen(
                            viewModel(
                                factory = GameMenuStatesViewModel.Factory(
                                    application,
                                    gameMenuRequest,
                                    statesManager,
                                    false,
                                    statesPreviewManager
                                )
                            ),
                            onStateClicked = {
                                onResult { putExtra(GameMenuContract.RESULT_SAVE, it) }
                            }
                        )
                    }
                    composable(GameMenuRoute.LOAD) {
                        GameMenuStatesScreen(
                            viewModel(
                                factory = GameMenuStatesViewModel.Factory(
                                    application,
                                    gameMenuRequest,
                                    statesManager,
                                    true,
                                    statesPreviewManager
                                )
                            ),
                            onStateClicked = {
                                onResult { putExtra(GameMenuContract.RESULT_LOAD, it) }
                            }
                        )
                    }
                    composable(GameMenuRoute.OPTIONS) {
                        GameMenuCoreOptionsScreen(
                            viewModel(
                                factory = GameMenuCoreOptionsViewModel.Factory(inputDeviceManager)
                            ),
                            gameMenuRequest
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SideMenu(content: @Composable () -> Unit) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {

            val panelWidth = remember(maxWidth) {
                minOf(maxWidth * 0.8f, 400f.dp)
            }

            Surface(
                modifier = Modifier
                    .padding()
                    .fillMaxHeight()
                    .width(panelWidth)
                    .clip(MaterialTheme.shapes.large)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }
    }

    private fun onResult(block: Intent.() -> Unit) {
        val resultIntent = Intent()
        resultIntent.block()
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    @dagger.Module
    abstract class Module
}
