package com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuRoute
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.composable
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.navigateToRoute
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.common.kotlin.serializable
import com.swordfish.lemuroid.lib.android.RetrogradeComponentActivity
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import java.security.InvalidParameterException
import javax.inject.Inject

class GameCheatsActivity : RetrogradeComponentActivity() {
    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    companion object {
        private const val EXTRA_GAME = "com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats.EXTRA_GAME"

        fun newIntent(
            context: Context,
            game: Game,
        ): Intent {
            return Intent(context, GameCheatsActivity::class.java).apply {
                putExtra(EXTRA_GAME, game)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            SystemBarStyle.dark(Color.TRANSPARENT),
            SystemBarStyle.dark(Color.TRANSPARENT),
        )

        val game =
            intent.serializable<Game>(EXTRA_GAME)
                ?: throw InvalidParameterException("Missing EXTRA_GAME")

        setContent {
            GameCheatsContent(game)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun GameCheatsContent(game: Game) {
        AppTheme {
            val navController = rememberNavController()
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry.value?.destination

            val currentRoute =
                currentDestination?.route
                    ?.let { GameMenuRoute.findByRoute(it) }
                    ?: GameMenuRoute.CHEATS

            val cheatsViewModel: GameMenuCheatsViewModel =
                viewModel(
                    viewModelStoreOwner = this@GameCheatsActivity,
                    factory = GameMenuCheatsViewModel.Factory(applicationContext, game, retrogradeDb.cheatDao()),
                )

            val canGoBack = navController.previousBackStackEntry != null

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = stringResource(currentRoute.titleId),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                if (currentRoute == GameMenuRoute.CHEATS) {
                                    Text(
                                        text = game.customName ?: game.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (canGoBack) {
                                    navController.popBackStack()
                                } else {
                                    finish()
                                }
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back),
                                )
                            }
                        },
                        actions = {
                            if (currentRoute == GameMenuRoute.CHEATS) {
                                CheatsOverflowMenu(viewModel = cheatsViewModel)
                            }
                        },
                    )
                },
            ) { paddingValues ->
                NavHost(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    navController = navController,
                    startDestination = GameMenuRoute.CHEATS.route,
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() },
                ) {
                    composable(GameMenuRoute.CHEATS) {
                        GameMenuCheatsScreen(
                            viewModel = cheatsViewModel,
                            onAddCheatClicked = {
                                cheatsViewModel.prepareAddCheat()
                                navController.navigateToRoute(GameMenuRoute.ADD_CHEAT)
                            },
                            onEditCheatClicked = { cheat ->
                                cheatsViewModel.prepareEditCheat(cheat)
                                navController.navigateToRoute(GameMenuRoute.EDIT_CHEAT)
                            },
                        )
                    }
                    composable(GameMenuRoute.ADD_CHEAT) {
                        GameMenuEditCheatScreen(
                            viewModel = cheatsViewModel,
                            systemId = game.systemId,
                            onSaved = { navController.popBackStack() },
                        )
                    }
                    composable(GameMenuRoute.EDIT_CHEAT) {
                        GameMenuEditCheatScreen(
                            viewModel = cheatsViewModel,
                            systemId = game.systemId,
                            onSaved = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
