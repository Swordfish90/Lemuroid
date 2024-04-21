package com.swordfish.lemuroid.app.mobile.feature.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesScreen
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesViewModel
import com.swordfish.lemuroid.app.mobile.feature.games.GamesScreen
import com.swordfish.lemuroid.app.mobile.feature.games.GamesViewModel
import com.swordfish.lemuroid.app.mobile.feature.home.HomeScreen
import com.swordfish.lemuroid.app.mobile.feature.home.HomeViewModel
import com.swordfish.lemuroid.app.mobile.feature.search.SearchScreen
import com.swordfish.lemuroid.app.mobile.feature.search.SearchViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.advanced.AdvancedSettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.advanced.AdvancedSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.bios.BiosScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.bios.BiosSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.coreselection.CoresSelectionScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.coreselection.CoresSelectionViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.general.SettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.general.SettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices.InputDevicesSettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices.InputDevicesSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.savesync.SaveSyncSettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.savesync.SaveSyncSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.shortcuts.ShortcutsGenerator
import com.swordfish.lemuroid.app.mobile.feature.systems.MetaSystemsScreen
import com.swordfish.lemuroid.app.mobile.feature.systems.MetaSystemsViewModel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidScaffold
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.shared.game.GameLauncher
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.main.BusyActivity
import com.swordfish.lemuroid.app.shared.main.GameLaunchTaskHandler
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues
import com.swordfish.lemuroid.common.coroutines.safeLaunch
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.android.RetrogradeComponentActivity
import com.swordfish.lemuroid.lib.bios.BiosManager
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import dagger.Provides
import de.charlex.compose.material3.HtmlText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : RetrogradeComponentActivity(), BusyActivity {
    @Inject
    lateinit var gameLaunchTaskHandler: GameLaunchTaskHandler

    @Inject
    lateinit var saveSyncManager: SaveSyncManager

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var gameInteractor: GameInteractor

    @Inject
    lateinit var biosManager: BiosManager

    @Inject
    lateinit var coresSelection: CoresSelection

    @Inject
    lateinit var settingsInteractor: SettingsInteractor

    @Inject
    lateinit var inputDeviceManager: InputDeviceManager

    private val reviewManager = ReviewManager()

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(applicationContext, saveSyncManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.safeLaunch {
            reviewManager.initialize(applicationContext)
        }

        setContent {
            val navController = rememberNavController()
            MainScreen(navController)
        }
    }

    @Composable
    private fun MainScreen(navController: NavHostController) {
        AppTheme {
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry.value?.destination
            val currentRoute =
                currentDestination?.route
                    ?.let { MainRoute.findByRoute(it) }

            val infoDialogDisplayed =
                remember {
                    mutableStateOf(false)
                }

            LaunchedEffect(currentRoute) {
                mainViewModel.update()
            }

            val selectedGameState =
                remember {
                    mutableStateOf<Game?>(null)
                }

            val onGameLongClick = { game: Game ->
                selectedGameState.value = game
            }

            val onGameClick = { game: Game ->
                gameInteractor.onGamePlay(game)
            }

            val onGameFavoriteToggle = { game: Game, isFavorite: Boolean ->
                gameInteractor.onFavoriteToggle(game, isFavorite)
            }

            val onHelpPressed = {
                infoDialogDisplayed.value = true
            }

            val mainUIState =
                mainViewModel.state
                    .observeAsState(MainViewModel.UiState())
                    .value

            // TODO COMPOSE Get rid of this double scaffold.
            Scaffold(
                bottomBar = { MainNavigationBar(currentRoute, navController) },
            ) { outerPadding ->

                @Composable
                fun Page(
                    mainRoute: MainRoute,
                    content: @Composable (MergedPaddingValues) -> Unit,
                ) {
                    LemuroidScaffold(
                        mainRoute,
                        navController,
                        mainUIState,
                        outerPadding,
                        content,
                        onHelpPressed,
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = MainRoute.HOME.route,
                ) {
                    composable(MainRoute.HOME) {
                        Page(MainRoute.HOME) { padding ->
                            HomeScreen(
                                padding,
                                viewModel(
                                    factory =
                                        HomeViewModel.Factory(
                                            applicationContext,
                                            retrogradeDb,
                                            settingsInteractor,
                                        ),
                                ),
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                            )
                        }
                    }
                    composable(MainRoute.FAVORITES) {
                        Page(MainRoute.FAVORITES) { padding ->
                            FavoritesScreen(
                                padding,
                                viewModel(
                                    factory = FavoritesViewModel.Factory(retrogradeDb),
                                ),
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                            )
                        }
                    }
                    composable(MainRoute.SEARCH) {
                        SearchScreen(
                            outerPadding,
                            navController,
                            viewModel(
                                factory = SearchViewModel.Factory(retrogradeDb),
                            ),
                            mainUIState = mainUIState,
                            onGameClick = onGameClick,
                            onGameLongClick = onGameLongClick,
                            onGameFavoriteToggle = onGameFavoriteToggle,
                            onHelpPressed = onHelpPressed,
                        )
                    }
                    composable(MainRoute.SYSTEMS) {
                        Page(MainRoute.SYSTEMS) { padding ->
                            MetaSystemsScreen(
                                padding,
                                navController,
                                viewModel(
                                    factory =
                                        MetaSystemsViewModel.Factory(
                                            retrogradeDb,
                                            applicationContext,
                                        ),
                                ),
                            )
                        }
                    }
                    composable(MainRoute.SYSTEM_GAMES) { entry ->
                        Page(MainRoute.SYSTEM_GAMES) { padding ->
                            val metaSystemId = entry.arguments?.getString("metaSystemId")
                            GamesScreen(
                                padding,
                                viewModel =
                                    viewModel(
                                        factory =
                                            GamesViewModel.Factory(
                                                retrogradeDb,
                                                MetaSystemID.valueOf(metaSystemId!!),
                                            ),
                                    ),
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                                onGameFavoriteToggle = onGameFavoriteToggle,
                            )
                        }
                    }
                    composable(MainRoute.SETTINGS) {
                        Page(MainRoute.SETTINGS) { padding ->
                            SettingsScreen(
                                padding,
                                viewModel =
                                    viewModel(
                                        factory =
                                            SettingsViewModel.Factory(
                                                applicationContext,
                                                settingsInteractor,
                                                saveSyncManager,
                                                FlowSharedPreferences(
                                                    SharedPreferencesHelper.getLegacySharedPreferences(
                                                        applicationContext,
                                                    ),
                                                ),
                                            ),
                                    ),
                                navController = navController,
                            )
                        }
                    }
                    composable(MainRoute.SETTINGS_ADVANCED) {
                        Page(MainRoute.SETTINGS_ADVANCED) { padding ->
                            AdvancedSettingsScreen(
                                padding,
                                viewModel =
                                    viewModel(
                                        factory =
                                            AdvancedSettingsViewModel.Factory(
                                                applicationContext,
                                                settingsInteractor,
                                            ),
                                    ),
                                navController,
                            )
                        }
                    }
                    composable(MainRoute.SETTINGS_BIOS) {
                        Page(MainRoute.SETTINGS_BIOS) { padding ->
                            BiosScreen(
                                padding,
                                viewModel =
                                    viewModel(
                                        factory = BiosSettingsViewModel.Factory(biosManager),
                                    ),
                            )
                        }
                    }
                    composable(MainRoute.SETTINGS_CORES_SELECTION) {
                        Page(MainRoute.SETTINGS_CORES_SELECTION) { padding ->
                            CoresSelectionScreen(
                                padding,
                                viewModel =
                                    viewModel(
                                        factory =
                                            CoresSelectionViewModel.Factory(
                                                applicationContext,
                                                coresSelection,
                                            ),
                                    ),
                            )
                        }
                    }
                    composable(MainRoute.SETTINGS_INPUT_DEVICES) {
                        Page(MainRoute.SETTINGS_INPUT_DEVICES) { padding ->
                            InputDevicesSettingsScreen(
                                padding,
                                viewModel =
                                    viewModel(
                                        factory =
                                            InputDevicesSettingsViewModel.Factory(
                                                applicationContext,
                                                inputDeviceManager,
                                            ),
                                    ),
                            )
                        }
                    }
                    composable(MainRoute.SETTINGS_SAVE_SYNC) {
                        Page(MainRoute.SETTINGS_SAVE_SYNC) { padding ->
                            SaveSyncSettingsScreen(
                                padding,
                                viewModel =
                                    viewModel(
                                        factory =
                                            SaveSyncSettingsViewModel.Factory(
                                                application,
                                                saveSyncManager,
                                            ),
                                    ),
                            )
                        }
                    }
                }
            }

            MainGameContextActions(
                selectedGameState = selectedGameState,
                shortcutSupported = gameInteractor.supportShortcuts(),
                onGamePlay = { gameInteractor.onGamePlay(it) },
                onGameRestart = { gameInteractor.onGameRestart(it) },
                onFavoriteToggle = { game: Game, isFavorite: Boolean ->
                    gameInteractor.onFavoriteToggle(game, isFavorite)
                },
                onCreateShortcut = { gameInteractor.onCreateShortcut(it) },
            )

            if (infoDialogDisplayed.value) {
                val message =
                    remember {
                        val systemFolders =
                            SystemID.values()
                                .joinToString(", ") { "<i>${it.dbname}</i>" }

                        getString(R.string.lemuroid_help_content)
                            .replace("\$SYSTEMS", systemFolders)
                    }

                AlertDialog(
                    text = { HtmlText(text = message) },
                    onDismissRequest = { infoDialogDisplayed.value = false },
                    confirmButton = { },
                )
            }
        }
    }

    override fun activity(): Activity = this

    override fun isBusy(): Boolean = mainViewModel.state.value?.operationInProgress ?: false

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            BaseGameActivity.REQUEST_PLAY_GAME -> {
                GlobalScope.safeLaunch {
                    gameLaunchTaskHandler.handleGameFinish(
                        true,
                        this@MainActivity,
                        resultCode,
                        data,
                    )
                }
            }
        }
    }

    @dagger.Module
    abstract class Module {
        @dagger.Module
        companion object {
            @Provides
            @PerActivity
            @JvmStatic
            fun settingsInteractor(
                activity: MainActivity,
                directoriesManager: DirectoriesManager,
            ) = SettingsInteractor(activity, directoriesManager)

            @Provides
            @PerActivity
            @JvmStatic
            fun gameInteractor(
                activity: MainActivity,
                retrogradeDb: RetrogradeDatabase,
                shortcutsGenerator: ShortcutsGenerator,
                gameLauncher: GameLauncher,
            ) = GameInteractor(activity, retrogradeDb, false, shortcutsGenerator, gameLauncher)
        }
    }
}
