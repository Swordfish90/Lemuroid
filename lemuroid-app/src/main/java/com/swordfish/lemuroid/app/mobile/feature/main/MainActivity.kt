package com.swordfish.lemuroid.app.mobile.feature.main

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesScreen
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesViewModel
import com.swordfish.lemuroid.app.mobile.feature.gamedetails.GameDetailScreen
import com.swordfish.lemuroid.app.mobile.feature.gamedetails.GameDetailViewModel
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.cheats.GameCheatsActivity
import com.swordfish.lemuroid.app.mobile.feature.games.GameSortOrder
import com.swordfish.lemuroid.app.mobile.feature.games.GamesScreen
import com.swordfish.lemuroid.app.mobile.feature.games.GamesViewModel
import com.swordfish.lemuroid.app.mobile.feature.home.HomeScreen
import com.swordfish.lemuroid.app.mobile.feature.home.HomeViewModel
import com.swordfish.lemuroid.app.mobile.feature.main.navigateToGameDetails
import com.swordfish.lemuroid.app.mobile.feature.search.SearchScreen
import com.swordfish.lemuroid.app.mobile.feature.search.SearchViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.SettingsManager
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
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.shared.game.GameProcessLock
import com.swordfish.lemuroid.app.shared.game.GameLauncher
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.main.BusyActivity
import com.swordfish.lemuroid.app.shared.main.GameLaunchTaskHandler
import com.swordfish.lemuroid.app.shared.scraper.BatchScraperScheduler
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.common.coroutines.safeLaunch
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.android.RetrogradeComponentActivity
import com.swordfish.lemuroid.lib.bios.BiosManager
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.covers.CustomCoverManager
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.db.entity.GameCollection
import com.swordfish.lemuroid.lib.library.db.entity.GameCollectionCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    @Inject
    lateinit var settingsManager: SettingsManager

    private val reviewManager = ReviewManager()

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(applicationContext, saveSyncManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            SystemBarStyle.dark(Color.TRANSPARENT),
            SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

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
                currentDestination?.route
                    ?.let { MainRoute.findByRoute(it) }
                    ?: MainRoute.HOME

            val infoDialogDisplayed =
                remember {
                    mutableStateOf(false)
                }

            var lastDestinationChangeMs by remember { mutableStateOf(0L) }

            LaunchedEffect(navBackStackEntry.value?.id) {
                lastDestinationChangeMs = SystemClock.uptimeMillis()
            }

            LaunchedEffect(currentRoute) {
                mainViewModel.changeRoute(currentRoute)
            }

            val selectedGameState =
                remember {
                    mutableStateOf<Game?>(null)
                }

            val onGameLongClick = { game: Game ->
                val isResumed = navController.currentBackStackEntry?.lifecycle?.currentState ==
                    androidx.lifecycle.Lifecycle.State.RESUMED
                if (isResumed && SystemClock.uptimeMillis() - lastDestinationChangeMs >= 250L) {
                    selectedGameState.value = game
                }
            }

            val directPlayOnTap = settingsManager.directPlayOnTapFlow()
                .collectAsState(initial = false)
                .value

            val onGameClick = { game: Game ->
                val isResumed = navController.currentBackStackEntry?.lifecycle?.currentState ==
                    androidx.lifecycle.Lifecycle.State.RESUMED
                if (isResumed && SystemClock.uptimeMillis() - lastDestinationChangeMs >= 250L) {
                    if (directPlayOnTap) {
                        gameInteractor.onGamePlay(game)
                    } else {
                        navController.navigateToGameDetails(game.id)
                    }
                }
            }

            val onGameFavoriteToggle = { game: Game, isFavorite: Boolean ->
                gameInteractor.onFavoriteToggle(game, isFavorite)
            }

            val onHelpPressed = {
                infoDialogDisplayed.value = true
            }

            var showBatchScrapeDialog by remember { mutableStateOf(false) }
            var showBatchScrapeAllDialog by remember { mutableStateOf(false) }
            var metaSystemForScrape by remember { mutableStateOf<MetaSystemID?>(null) }
            var overwriteExistingScrape by remember { mutableStateOf(false) }
            var isSystemSearchActive by remember { mutableStateOf(false) }
            var systemSearchQuery by remember { mutableStateOf("") }
            var isFavoritesSearchActive by remember { mutableStateOf(false) }
            var favoritesSearchQuery by remember { mutableStateOf("") }
            var selectedSystemGenres by remember { mutableStateOf<Set<String>>(emptySet()) }
            var showSortDialog by remember { mutableStateOf(false) }
            var gameForCollectionDialog by remember { mutableStateOf<Game?>(null) }

            val sortOrderKey by settingsManager.gameSortOrderFlow()
                .collectAsState(initial = GameSortOrder.NAME_ASC.name)
            val currentSortOrder = remember(sortOrderKey) {
                GameSortOrder.fromKey(sortOrderKey)
            }

            val gameViewMode by settingsManager.gameViewModeFlow()
                .collectAsState(initial = "LIST")
            val isGridView = gameViewMode.equals("GRID", ignoreCase = true)

            LaunchedEffect(currentRoute) {
                if (currentRoute != MainRoute.SYSTEM_GAMES) {
                    isSystemSearchActive = false
                    systemSearchQuery = ""
                }
                if (currentRoute != MainRoute.FAVORITES) {
                    isFavoritesSearchActive = false
                    favoritesSearchQuery = ""
                }
                if (currentRoute != MainRoute.SYSTEM_GAMES && currentRoute != MainRoute.GAME_DETAILS) {
                    selectedSystemGenres = emptySet()
                }
            }

            BackHandler(enabled = currentRoute == MainRoute.SYSTEM_GAMES && isSystemSearchActive) {
                isSystemSearchActive = false
                systemSearchQuery = ""
            }

            BackHandler(enabled = currentRoute == MainRoute.FAVORITES && isFavoritesSearchActive) {
                isFavoritesSearchActive = false
                favoritesSearchQuery = ""
            }

            val onScrapePlatformPressed = {
                val metaSystemIdArg = navBackStackEntry.value?.arguments?.getString("metaSystemId")
                val meta = runCatching {
                    MetaSystemID.valueOf(metaSystemIdArg ?: "")
                }.getOrNull()
                if (meta != null) {
                    metaSystemForScrape = meta
                    showBatchScrapeDialog = true
                }
            }

            val onCancelScrapePlatform = {
                BatchScraperScheduler.cancelBatchScrape(applicationContext)
                Toast.makeText(
                    applicationContext,
                    getString(R.string.toast_scrape_cancelled),
                    Toast.LENGTH_SHORT,
                ).show()
            }

            val mainUIState =
                mainViewModel.state
                    .collectAsState(MainViewModel.UiState())
                    .value

            val currentSystemTitle = remember(currentRoute, navBackStackEntry.value) {
                if (currentRoute == MainRoute.SYSTEM_GAMES) {
                    val metaSystemIdArg = navBackStackEntry.value?.arguments?.getString("metaSystemId")
                    runCatching { MetaSystemID.valueOf(metaSystemIdArg ?: "") }
                        .getOrNull()
                        ?.titleResId
                        ?.let { getString(it) }
                } else {
                    null
                }
            }

            Scaffold(
                topBar = {
                    if (currentRoute.showTopBar) {
                        MainTopBar(
                            currentRoute = currentRoute,
                            navController = navController,
                            onHelpPressed = onHelpPressed,
                            mainUIState = mainUIState,
                            titleOverride = currentSystemTitle,
                            onUpdateQueryString = { mainViewModel.changeQueryString(it) },
                            onScrapePlatformPressed = onScrapePlatformPressed,
                            onScrapeAllPlatformsPressed = { showBatchScrapeAllDialog = true },
                            onCancelScrapePlatform = onCancelScrapePlatform,
                            isSystemSearchActive = isSystemSearchActive,
                            systemSearchQuery = systemSearchQuery,
                            onSystemSearchToggle = { active ->
                                isSystemSearchActive = active
                                if (!active) systemSearchQuery = ""
                            },
                            onSystemSearchQueryChange = { systemSearchQuery = it },
                            isFavoritesSearchActive = isFavoritesSearchActive,
                            favoritesSearchQuery = favoritesSearchQuery,
                            onFavoritesSearchToggle = { active ->
                                isFavoritesSearchActive = active
                                if (!active) favoritesSearchQuery = ""
                            },
                            onFavoritesSearchQueryChange = { favoritesSearchQuery = it },
                            onSortPressed = { showSortDialog = true },
                            isGridView = isGridView,
                            onToggleViewMode = {
                                settingsManager.setGameViewMode(if (isGridView) "LIST" else "GRID")
                            },
                        )
                    }
                },
                bottomBar = {
                    if (currentRoute.showBottomNavigation) {
                        MainNavigationBar(currentRoute, navController)
                    }
                },
            ) { padding ->
                NavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    startDestination = MainRoute.HOME.route,
                    enterTransition = { fadeIn(animationSpec = tween(180)) },
                    exitTransition = { fadeOut(animationSpec = tween(180)) },
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
                            onGamePlay = { gameInteractor.onGamePlay(it) },
                            onGameLongClick = onGameLongClick,
                            onGameFavoriteToggle = onGameFavoriteToggle,
                            onOpenCoreSelection = { navController.navigateToRoute(MainRoute.SETTINGS_CORES_SELECTION) },
                        )
                    }
                    composable(MainRoute.FAVORITES) {
                        FavoritesScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                                viewModel(
                                    factory = FavoritesViewModel.Factory(retrogradeDb),
                                ),
                            searchQuery = favoritesSearchQuery,
                            sortOrder = currentSortOrder,
                            isGridView = isGridView,
                            onGameClick = onGameClick,
                            onGameLongClick = onGameLongClick,
                            onGameFavoriteToggle = onGameFavoriteToggle,
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
                        MetaSystemsScreen(
                            modifier = Modifier.padding(padding),
                            navController = navController,
                            viewModel =
                                viewModel(
                                    factory =
                                        MetaSystemsViewModel.Factory(
                                            retrogradeDb,
                                            applicationContext,
                                        ),
                                ),
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
                            searchQuery = if (isSystemSearchActive) systemSearchQuery else "",
                            selectedGenres = selectedSystemGenres,
                            onGenresChange = { selectedSystemGenres = it },
                            sortOrder = currentSortOrder,
                            isGridView = isGridView,
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
                    composable(MainRoute.GAME_DETAILS) { entry ->
                        val gameId = entry.arguments?.getInt("gameId") ?: return@composable
                        GameDetailScreen(
                            modifier = Modifier.fillMaxSize(),
                            viewModel =
                                viewModel(
                                    factory =
                                        GameDetailViewModel.Factory(
                                            gameId = gameId,
                                            retrogradeDb = retrogradeDb,
                                            settingsManager = settingsManager,
                                            appContext = applicationContext,
                                        ),
                                ),
                            onBack = { navController.popBackStack() },
                            onPlayGame = { gameInteractor.onGamePlay(it) },
                            onOpenCheats = { startActivity(GameCheatsActivity.newIntent(this@MainActivity, it)) },
                            onGenreClick = { clickedGame, genre ->
                                selectedSystemGenres = setOf(genre)
                                val prevRoute = navController.previousBackStackEntry?.destination?.route
                                if (prevRoute == MainRoute.SYSTEM_GAMES.route) {
                                    navController.popBackStack()
                                } else {
                                    val meta = MetaSystemID.values().firstOrNull { m ->
                                        m.systemIDs.any { it.dbname == clickedGame.systemId }
                                    }
                                    if (meta != null) {
                                        navController.navigate(
                                            MainRoute.SYSTEM_GAMES.route.replace("{metaSystemId}", meta.name),
                                        )
                                    } else {
                                        navController.popBackStack()
                                    }
                                }
                            },
                        )
                    }
                }
            }

            MainGameContextActions(
                selectedGameState = selectedGameState,
                shortcutSupported = gameInteractor.supportShortcuts(),
                onGamePlay = { gameInteractor.onGamePlay(it) },
                onGameRestart = { gameInteractor.onGameRestart(it) },
                onGameInfo = { navController.navigateToGameDetails(it.id) },
                onManageCheats = { game ->
                    startActivity(GameCheatsActivity.newIntent(this@MainActivity, game))
                },
                onFavoriteToggle = { game: Game, isFavorite: Boolean ->
                    gameInteractor.onFavoriteToggle(game, isFavorite)
                },
                onAddToCollection = { game -> gameForCollectionDialog = game },
                onCreateShortcut = { gameInteractor.onCreateShortcut(it) },
                onSetCustomName = { game, name -> gameInteractor.onSetCustomName(game, name) },
                onClearCustomName = { game -> gameInteractor.onClearCustomName(game) },
                onSetCustomCoverUri = { game, uri -> gameInteractor.onSetCustomCoverUri(game, uri) },
                onClearCustomCoverUri = { game -> gameInteractor.onClearCustomCoverUri(game) },
                onDeleteGame = { game -> gameInteractor.onDeleteGame(game) },
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

            if (showBatchScrapeDialog && metaSystemForScrape != null) {
                val meta = metaSystemForScrape!!
                val systemTitle = stringResource(id = meta.titleResId)
                AlertDialog(
                    onDismissRequest = { showBatchScrapeDialog = false },
                    title = {
                        Text(text = stringResource(R.string.dialog_scrape_platform_title, systemTitle))
                    },
                    text = {
                        Column {
                            Text(
                                text = stringResource(R.string.dialog_scrape_platform_message, systemTitle),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        role = Role.Checkbox,
                                        value = overwriteExistingScrape,
                                        onValueChange = { overwriteExistingScrape = it },
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = overwriteExistingScrape,
                                    onCheckedChange = null,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.dialog_scrape_platform_overwrite),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showBatchScrapeDialog = false
                                BatchScraperScheduler.scheduleBatchScrape(
                                    applicationContext = applicationContext,
                                    metaSystemID = meta,
                                    overwrite = overwriteExistingScrape,
                                )
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.toast_scrape_started, systemTitle),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            },
                        ) {
                            Text(stringResource(R.string.dialog_scrape_platform_start))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBatchScrapeDialog = false }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                    },
                )
            }

            if (showBatchScrapeAllDialog) {
                AlertDialog(
                    onDismissRequest = { showBatchScrapeAllDialog = false },
                    title = {
                        Text(text = stringResource(R.string.scrape_all_platforms))
                    },
                    text = {
                        Column {
                            Text(
                                text = stringResource(R.string.dialog_scrape_all_message),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        role = Role.Checkbox,
                                        value = overwriteExistingScrape,
                                        onValueChange = { overwriteExistingScrape = it },
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = overwriteExistingScrape,
                                    onCheckedChange = null,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.dialog_scrape_platform_overwrite),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showBatchScrapeAllDialog = false
                                BatchScraperScheduler.scheduleBatchScrapeAll(
                                    applicationContext = applicationContext,
                                    overwrite = overwriteExistingScrape,
                                )
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.toast_scrape_all_started),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            },
                        ) {
                            Text(stringResource(R.string.dialog_scrape_platform_start))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBatchScrapeAllDialog = false }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                    },
                )
            }

            if (showSortDialog) {
                AlertDialog(
                    onDismissRequest = { showSortDialog = false },
                    title = { Text(text = stringResource(R.string.menu_sort_by)) },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            GameSortOrder.values().forEach { option ->
                                val selected = option == currentSortOrder
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selected,
                                            role = Role.RadioButton,
                                            onClick = {
                                                settingsManager.setGameSortOrder(option.name)
                                                showSortDialog = false
                                            },
                                        )
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = null,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = stringResource(id = option.labelResId),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSortDialog = false }) {
                            Text(stringResource(android.R.string.cancel))
                        }
                    },
                )
            }

            gameForCollectionDialog?.let { targetGame ->
                val allCollections by retrogradeDb.collectionDao()
                    .getAllCollections()
                    .collectAsState(initial = emptyList())
                val gameCollectionIds by retrogradeDb.collectionDao()
                    .getCollectionIdsForGame(targetGame.id)
                    .collectAsState(initial = emptyList())
                var showInlineCreate by remember { mutableStateOf(false) }
                var newCollectionName by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { gameForCollectionDialog = null },
                    title = {
                        Text(text = stringResource(R.string.game_context_menu_add_to_collection))
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            if (allCollections.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.dialog_add_to_collection_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                allCollections.forEach { collection ->
                                    val isInCollection = gameCollectionIds.contains(collection.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .toggleable(
                                                value = isInCollection,
                                                role = Role.Checkbox,
                                                onValueChange = { checked ->
                                                    lifecycleScope.launch(Dispatchers.IO) {
                                                        if (checked) {
                                                            retrogradeDb.collectionDao().addGameToCollection(
                                                                GameCollectionCrossRef(
                                                                    collectionId = collection.id,
                                                                    gameId = targetGame.id,
                                                                ),
                                                            )
                                                        } else {
                                                            retrogradeDb.collectionDao().removeGameFromCollection(
                                                                collectionId = collection.id,
                                                                gameId = targetGame.id,
                                                            )
                                                        }
                                                    }
                                                },
                                            )
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Checkbox(
                                            checked = isInCollection,
                                            onCheckedChange = null,
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = collection.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            }

                            if (showInlineCreate) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = newCollectionName,
                                    onValueChange = { newCollectionName = it },
                                    singleLine = true,
                                    label = { Text(stringResource(R.string.collection_name_hint)) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    },
                    confirmButton = {
                        if (showInlineCreate) {
                            TextButton(
                                enabled = newCollectionName.trim().isNotEmpty(),
                                onClick = {
                                    val trimmed = newCollectionName.trim()
                                    if (trimmed.isNotEmpty()) {
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            val newId = retrogradeDb.collectionDao()
                                                .insertCollection(GameCollection(name = trimmed))
                                                .toInt()
                                            retrogradeDb.collectionDao().addGameToCollection(
                                                GameCollectionCrossRef(
                                                    collectionId = newId,
                                                    gameId = targetGame.id,
                                                ),
                                            )
                                        }
                                        newCollectionName = ""
                                        showInlineCreate = false
                                    }
                                },
                            ) {
                                Text(stringResource(R.string.collection_create_action))
                            }
                        } else {
                            TextButton(onClick = { gameForCollectionDialog = null }) {
                                Text(stringResource(android.R.string.ok))
                            }
                        }
                    },
                    dismissButton = {
                        if (showInlineCreate) {
                            TextButton(onClick = {
                                showInlineCreate = false
                                newCollectionName = ""
                            }) {
                                Text(stringResource(android.R.string.cancel))
                            }
                        } else {
                            TextButton(onClick = { showInlineCreate = true }) {
                                Text(stringResource(R.string.collection_create_title))
                            }
                        }
                    },
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
                val isRestart = data?.extras?.getBoolean(BaseGameActivity.PLAY_GAME_RESULT_RESTART) == true
                GlobalScope.safeLaunch {
                    gameLaunchTaskHandler.handleGameFinish(
                        !isRestart,
                        this@MainActivity,
                        resultCode,
                        data,
                    )
                    if (isRestart) {
                        val game = data?.extras?.getSerializable(BaseGameActivity.PLAY_GAME_RESULT_GAME) as? Game
                        if (game != null) {
                            val isLeanback = data.extras?.getBoolean(BaseGameActivity.PLAY_GAME_RESULT_LEANBACK) == true
                            var attempts = 0
                            while (attempts < 25 && GameProcessLock.isHeldByAnotherProcess(applicationContext)) {
                                delay(100)
                                attempts++
                            }
                            withContext(Dispatchers.Main) {
                                gameInteractor.onGameRestart(game)
                            }
                        }
                    }
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
