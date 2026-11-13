// Note to developers who edit this code: I used AI to help optimize this code and make it much better,
// because I am a newcomer to this. Please keep that in mind when reading or changing it.

package come.asg.ominoy.app.mobile.feature.main

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager
import come.asg.ominoy.R
import come.asg.ominoy.app.mobile.feature.cheats.GameCheatsScreen
import come.asg.ominoy.app.mobile.feature.favorites.FavoritesScreen
import come.asg.ominoy.app.mobile.feature.favorites.FavoritesViewModel
import come.asg.ominoy.app.mobile.feature.games.GamesScreen
import come.asg.ominoy.app.mobile.feature.games.GamesViewModel
import come.asg.ominoy.app.mobile.feature.home.HomeScreen
import come.asg.ominoy.app.mobile.feature.home.HomeViewModel
import come.asg.ominoy.app.mobile.feature.library.LibraryScreen
import come.asg.ominoy.app.mobile.feature.library.LibraryViewModel
import come.asg.ominoy.app.mobile.feature.search.SearchScreen
import come.asg.ominoy.app.mobile.feature.search.SearchViewModel
import come.asg.ominoy.app.mobile.feature.settings.advanced.AdvancedSettingsScreen
import come.asg.ominoy.app.mobile.feature.settings.advanced.AdvancedSettingsViewModel
import come.asg.ominoy.app.mobile.feature.settings.bios.BiosScreen
import come.asg.ominoy.app.mobile.feature.settings.bios.BiosSettingsViewModel
import come.asg.ominoy.app.mobile.feature.settings.coreselection.CoresSelectionScreen
import come.asg.ominoy.app.mobile.feature.settings.coreselection.CoresSelectionViewModel
import come.asg.ominoy.app.mobile.feature.settings.general.SettingsScreen
import come.asg.ominoy.app.mobile.feature.settings.general.SettingsViewModel
import come.asg.ominoy.app.mobile.feature.settings.general.ViewportAlignmentScreen
import come.asg.ominoy.app.mobile.feature.settings.inputdevices.InputDevicesSettingsScreen
import come.asg.ominoy.app.mobile.feature.settings.inputdevices.InputDevicesSettingsViewModel
import come.asg.ominoy.app.mobile.feature.settings.inputdevices.TouchLayoutEditorScreen
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.RetroAchievementsScreen
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.RetroAchievementsViewModel
import come.asg.ominoy.app.mobile.feature.settings.savesync.SaveSyncSettingsScreen
import come.asg.ominoy.app.mobile.feature.settings.savesync.SaveSyncSettingsViewModel
import come.asg.ominoy.app.mobile.feature.shortcuts.ShortcutsGenerator
import come.asg.ominoy.app.mobile.shared.compose.ui.AppTheme
import come.asg.ominoy.app.shared.GameInteractor
import come.asg.ominoy.app.shared.game.BaseGameActivity
import come.asg.ominoy.app.shared.game.GameLauncher
import come.asg.ominoy.app.shared.input.InputDeviceManager
import come.asg.ominoy.app.shared.main.BusyActivity
import come.asg.ominoy.app.shared.main.GameLaunchTaskHandler
import come.asg.ominoy.app.shared.settings.SettingsInteractor
import come.asg.ominoy.common.coroutines.safeLaunch
import come.asg.ominoy.ext.feature.review.ReviewManager
import come.asg.ominoy.lib.android.RetrogradeComponentActivity
import come.asg.ominoy.lib.bios.BiosManager
import come.asg.ominoy.lib.core.CoresSelection
import come.asg.ominoy.lib.injection.PerActivity
import come.asg.ominoy.lib.library.MetaSystemID
import come.asg.ominoy.lib.library.covers.CustomCoverManager
import come.asg.ominoy.lib.library.db.RetrogradeDatabase
import come.asg.ominoy.lib.library.db.entity.Game
import come.asg.ominoy.lib.preferences.SharedPreferencesHelper
import come.asg.ominoy.lib.savesync.SaveSyncManager
import come.asg.ominoy.lib.storage.DirectoriesManager
import dagger.Provides
import de.charlex.compose.material3.HtmlText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import javax.inject.Inject

private const val OMINOY_GITHUB_URL = "https://github.com/Amarthya-sg/OMINOY"

@OptIn(DelicateCoroutinesApi::class)
class MainActivity :
    RetrogradeComponentActivity(),
    BusyActivity {
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
        enableEdgeToEdge(
            SystemBarStyle.dark(Color.TRANSPARENT),
            SystemBarStyle.dark(Color.TRANSPARENT),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        GlobalScope.safeLaunch {
            reviewManager.initialize(applicationContext)
        }

        setContent {
            val navController = rememberNavController()
            MainScreen(navController)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen(navController: NavHostController) {
        AppTheme {
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry.value?.destination
            val currentRoute =
                currentDestination
                    ?.route
                    ?.let { MainRoute.findByRoute(it) }
                    ?: MainRoute.HOME

            val infoDialogDisplayed =
                remember {
                    mutableStateOf(false)
                }

            LaunchedEffect(currentRoute) {
                mainViewModel.changeRoute(currentRoute)
                if (currentRoute != MainRoute.SETTINGS_TOUCH_LAYOUT) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
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
                    .collectAsState(MainViewModel.UiState())
                    .value

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentWindowInsets = WindowInsets(0.dp),
                topBar = {
                    if (currentRoute != MainRoute.SETTINGS_TOUCH_LAYOUT &&
                        currentRoute != MainRoute.SETTINGS_VIEWPORT_ALIGNMENT &&
                        currentRoute != MainRoute.HOME &&
                        currentRoute != MainRoute.SYSTEMS &&
                        currentRoute != MainRoute.SETTINGS
                    ) {
                        MainTopBar(
                            currentRoute = currentRoute,
                            navController = navController,
                            onHelpPressed = onHelpPressed,
                            mainUIState = mainUIState,
                            onUpdateQueryString = { mainViewModel.changeQueryString(it) },
                        )
                    }
                },
            ) { padding ->
                Box(Modifier.fillMaxSize()) {
                    NavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        startDestination = MainRoute.HOME.route,
                    ) {
                        composable(MainRoute.HOME) {
                            HomeScreen(
                                modifier = Modifier.padding(padding),
                                viewModel =
                                    viewModel(
                                        factory =
                                            HomeViewModel.Factory(
                                                applicationContext,
                                                retrogradeDb,
                                                coresSelection,
                                            ),
                                    ),
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                                onGameFavoriteToggle = onGameFavoriteToggle,
                                onOpenCoreSelection = {
                                    navController.navigateToRoute(
                                        MainRoute.SETTINGS_CORES_SELECTION,
                                    )
                                },
                                onOpenSearch = { navController.navigateToRoute(MainRoute.SEARCH) },
                                onHelpPressed = onHelpPressed,
                            )
                        }
                        composable(MainRoute.FAVORITES) {
                            FavoritesScreen(
                                modifier = Modifier.padding(padding),
                                viewModel =
                                    viewModel(
                                        factory = FavoritesViewModel.Factory(retrogradeDb),
                                    ),
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                            )
                        }
                        composable(MainRoute.SEARCH) {
                            SearchScreen(
                                modifier = Modifier.padding(padding),
                                viewModel =
                                    viewModel(
                                        factory = SearchViewModel.Factory(retrogradeDb),
                                    ),
                                searchQuery = mainUIState.searchQuery,
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                                onGameFavoriteToggle = onGameFavoriteToggle,
                                onResetSearchQuery = { mainViewModel.changeQueryString("") },
                            )
                        }
                        composable(MainRoute.SYSTEMS) {
                            LibraryScreen(
                                modifier = Modifier.padding(padding),
                                viewModel =
                                    viewModel(
                                        factory = LibraryViewModel.Factory(retrogradeDb),
                                    ),
                                onGameClick = onGameClick,
                                onGameLongClick = onGameLongClick,
                                onGameFavoriteToggle = onGameFavoriteToggle,
                            )
                        }
                        composable(MainRoute.SYSTEM_GAMES) { entry ->
                            val metaSystemId = entry.arguments?.getString("metaSystemId")
                            GamesScreen(
                                modifier = Modifier.padding(padding),
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
                        composable(MainRoute.SETTINGS) {
                            SettingsScreen(
                                modifier = Modifier.padding(padding),
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
                        composable(MainRoute.SETTINGS_ADVANCED) {
                            AdvancedSettingsScreen(
                                modifier = Modifier.padding(padding),
                                viewModel =
                                    viewModel(
                                        factory =
                                            AdvancedSettingsViewModel.Factory(
                                                applicationContext,
                                                settingsInteractor,
                                            ),
                                    ),
                                navController = navController,
                            )
                        }
                        composable(MainRoute.SETTINGS_BIOS) {
                            BiosScreen(
                                modifier = Modifier.padding(padding),
                                viewModel =
                                    viewModel(
                                        factory = BiosSettingsViewModel.Factory(biosManager),
                                    ),
                            )
                        }
                        composable(MainRoute.SETTINGS_CORES_SELECTION) {
                            CoresSelectionScreen(
                                modifier = Modifier.padding(padding),
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
                        composable(MainRoute.SETTINGS_INPUT_DEVICES) {
                            InputDevicesSettingsScreen(
                                modifier = Modifier.padding(padding),
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
                        composable(MainRoute.SETTINGS_TOUCH_LAYOUT) {
                            val touchSettingsManager =
                                remember(applicationContext) {
                                    TouchControllerSettingsManager(
                                        SharedPreferencesHelper.getLegacySharedPreferences(applicationContext),
                                    )
                                }
                            TouchLayoutEditorScreen(
                                modifier = Modifier.fillMaxSize(),
                                settingsManager = touchSettingsManager,
                                viewportAlignment =
                                    SharedPreferencesHelper
                                        .getLegacySharedPreferences(applicationContext)
                                        .getString("viewport_alignment", "center") ?: "center",
                            )
                        }
                        composable(MainRoute.SETTINGS_VIEWPORT_ALIGNMENT) {
                            val touchSettingsManager =
                                remember(applicationContext) {
                                    TouchControllerSettingsManager(
                                        SharedPreferencesHelper.getLegacySharedPreferences(applicationContext),
                                    )
                                }
                            ViewportAlignmentScreen(
                                modifier = Modifier.fillMaxSize(),
                                preferences = SharedPreferencesHelper.getLegacySharedPreferences(applicationContext),
                                settingsManager = touchSettingsManager,
                            )
                        }
                        composable(MainRoute.SETTINGS_SAVE_SYNC) {
                            SaveSyncSettingsScreen(
                                modifier = Modifier.padding(padding),
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
                        composable(MainRoute.SETTINGS_RETROACHIEVEMENTS) {
                            RetroAchievementsScreen(
                                modifier = Modifier.padding(padding),
                                viewModel = viewModel(factory = RetroAchievementsViewModel.Factory(applicationContext)),
                                database = retrogradeDb,
                            )
                        }
                        composable(MainRoute.GAME_CHEATS) { entry ->
                            val gameId = entry.arguments?.getInt("gameId") ?: return@composable
                            val systemId = entry.arguments?.getString("systemId") ?: return@composable
                            GameCheatsScreen(
                                gameId = gameId,
                                systemId = systemId,
                                gameName = "",
                                gameCheatDao = retrogradeDb.gameCheatDao(),
                                modifier = Modifier.padding(padding),
                            )
                        }
                    }
                    if (currentRoute.showBottomNavigation && currentRoute != MainRoute.SETTINGS_TOUCH_LAYOUT) {
                        MainNavigationBar(
                            currentRoute = currentRoute,
                            navController = navController,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }

            MainGameContextActions(
                selectedGameState = selectedGameState,
                shortcutSupported = gameInteractor.supportShortcuts(),
                onGamePlay = { gameInteractor.onGamePlay(it) },
                onOpenCheats = { game ->
                    navController.navigate(
                        MainRoute.GAME_CHEATS.route
                            .replace("{gameId}", game.id.toString())
                            .replace("{systemId}", game.systemId),
                    )
                },
                onGameRestart = { gameInteractor.onGameRestart(it) },
                onFavoriteToggle = { game: Game, isFavorite: Boolean ->
                    gameInteractor.onFavoriteToggle(game, isFavorite)
                },
                onCreateShortcut = { gameInteractor.onCreateShortcut(it) },
                onSetCustomName = { game, name -> gameInteractor.onSetCustomName(game, name) },
                onClearCustomName = { game -> gameInteractor.onClearCustomName(game) },
                onSetCustomCoverUri = { game, uri -> gameInteractor.onSetCustomCoverUri(game, uri) },
                onClearCustomCoverUri = { game -> gameInteractor.onClearCustomCoverUri(game) },
            )

            if (infoDialogDisplayed.value) {
                val message =
                    remember {
                        val systemFolders =
                            "<i>Game Boy (GB)</i>, <i>Game Boy Color (GBC)</i>, and <i>Game Boy Advance (GBA)</i>"

                        getString(R.string.ominoy_help_content)
                            .replace("\$SYSTEMS", systemFolders)
                    }

                AlertDialog(
                    title = { Text(text = "Help & updates") },
                    text = { HtmlText(text = message) },
                    onDismissRequest = { infoDialogDisplayed.value = false },
                    confirmButton = {
                        Row {
                            TextButton(onClick = {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OMINOY_GITHUB_URL)))
                            }) {
                                Text(text = "GitHub updates")
                            }
                            TextButton(onClick = { infoDialogDisplayed.value = false }) {
                                Text(text = getString(android.R.string.ok))
                            }
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                )
            }
        }
    }

    override fun activity(): Activity = this

    override fun isBusy(): Boolean = mainViewModel.state.value.operationInProgress ?: false

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
                customCoverManager: CustomCoverManager,
            ) = GameInteractor(activity, retrogradeDb, false, shortcutsGenerator, gameLauncher, customCoverManager)
        }
    }
}
