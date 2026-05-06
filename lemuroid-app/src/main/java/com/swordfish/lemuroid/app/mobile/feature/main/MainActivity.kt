/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.app.mobile.feature.main

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.FulldiveConfigs
import com.swordfish.lemuroid.app.appextension.PopupManager
import com.swordfish.lemuroid.app.appextension.roomcord.ShareRoomcordTextGenerator
import com.swordfish.lemuroid.app.appextension.roomcord.ShareSuccessDialog
import com.swordfish.lemuroid.app.appextension.roomcord.ShowShareDialog
import com.swordfish.lemuroid.app.appextension.isFullRoidProInstalled
import com.swordfish.lemuroid.app.appextension.isProVersion
import com.swordfish.lemuroid.app.appextension.launchApp
import com.swordfish.lemuroid.app.appextension.openAppInGooglePlay
import com.swordfish.lemuroid.app.fulldive.analytics.IActionTracker
import com.swordfish.lemuroid.app.fulldive.analytics.TrackerConstants
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesScreen
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesViewModel
import com.swordfish.lemuroid.app.mobile.feature.games.GamesScreen
import com.swordfish.lemuroid.app.mobile.feature.games.GamesViewModel
import com.swordfish.lemuroid.app.mobile.feature.catalog.CatalogDetailScreen
import com.swordfish.lemuroid.app.mobile.feature.home.HomeScreen
import com.swordfish.lemuroid.app.mobile.feature.home.HomeViewModel
import com.swordfish.lemuroid.app.mobile.feature.proinfo.RoomcordPopupLayout
import com.swordfish.lemuroid.app.mobile.feature.proinfo.ProPopupLayout
import com.swordfish.lemuroid.app.mobile.feature.proinfo.tutorial.ProTutorialScreen
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
import com.swordfish.lemuroid.app.shared.catalog.CatalogSyncWork
import com.swordfish.lemuroid.app.mobile.feature.systems.MetaSystemsScreen
import com.swordfish.lemuroid.app.mobile.feature.systems.MetaSystemsViewModel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.shared.game.GameLauncher
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.main.BusyActivity
import com.swordfish.lemuroid.app.shared.main.GameLaunchTaskHandler
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.common.coroutines.safeLaunch
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.android.RetrogradeComponentActivity
import com.swordfish.lemuroid.lib.bios.BiosManager
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.library.GameSystemHelperImpl
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.citra.Citra3DSKeysManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import dagger.Provides
import de.charlex.compose.material3.HtmlText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    lateinit var citra3DSKeysManager: Citra3DSKeysManager

    @Inject
    lateinit var inputDeviceManager: InputDeviceManager

    @Inject
    lateinit var actionTracker: IActionTracker

    @Inject
    lateinit var shareRoomcordTextGenerator: ShareRoomcordTextGenerator

    @Inject
    lateinit var popupManager: PopupManager

    private val reviewManager = ReviewManager()

    private val _navigateToProTutorial = MutableStateFlow(false)
    private val navigateToProTutorial = _navigateToProTutorial.asStateFlow()

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

        CatalogSyncWork.schedule(applicationContext)

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

            val isProPopupVisible = remember {
                mutableStateOf(false)
            }
            val isRoomcordPopupVisible = remember {
                mutableStateOf(false)
            }
            if (currentRoute == MainRoute.HOME) {
                popupManager.onAppStarted(this@MainActivity)
                isProPopupVisible.value = popupManager.isProPopupVisible()
                isRoomcordPopupVisible.value = popupManager.isRoomcordPopupVisible()
            } else {
                isProPopupVisible.value = false
                isRoomcordPopupVisible.value = false
            }

            val infoDialogDisplayed =
                remember {
                    mutableStateOf(false)
                }

            val shareRoomcordDialogDisplayed =
                remember {
                    mutableStateOf<Game?>(null)
                }

            val shareSuccessVisible =
                remember {
                    mutableStateOf(false)
                }

            LaunchedEffect(currentRoute) {
                mainViewModel.changeRoute(currentRoute)
            }

            // Handle navigation to Pro Tutorial for 7z feature
            val shouldNavigateToProTutorial = navigateToProTutorial.collectAsState()
            LaunchedEffect(shouldNavigateToProTutorial.value) {
                if (shouldNavigateToProTutorial.value) {
                    navController.navigate(MainRoute.PRO_TUTORIAL.route)
                    _navigateToProTutorial.value = false
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
                topBar = {
                    MainTopBar(
                        currentRoute = currentRoute,
                        navController = navController,
                        onHelpPressed = onHelpPressed,
                        mainUIState = mainUIState,
                        onUpdateQueryString = { mainViewModel.changeQueryString(it) },
                        isProTutorialNavigationVisible = currentRoute != MainRoute.PRO_TUTORIAL && !isProVersion(),
                        onProButtonClick = {
                            if (packageManager.isFullRoidProInstalled()) {
                                launchApp(this@MainActivity, FulldiveConfigs.FULLROID_PRO_PACKAGE_NAME)
                            } else {
                                navController.navigate(MainRoute.PRO_TUTORIAL.route)
                            }
                        }
                    )
                },
                bottomBar = { MainNavigationBar(currentRoute, navController) },
            ) { padding ->
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
                            onOpenCoreSelection = { navController.navigateToRoute(MainRoute.SETTINGS_CORES_SELECTION) },
                            onCatalogGameClicked = { game ->
                                navController.navigate("catalog/${game.id}")
                            },
                        )
                        when {
                            isProPopupVisible.value -> {
                                actionTracker.logAction(TrackerConstants.EVENT_PRO_POPUP_SHOWN)
                                ProPopupLayout(
                                    bottomPadding = padding.calculateBottomPadding(),
                                    onClick = {
                                        actionTracker.logAction(TrackerConstants.EVENT_PRO_TUTORIAL_OPENED_FROM_PRO_POPUP)
                                        navController.navigateToRoute(MainRoute.PRO_TUTORIAL)
                                        isProPopupVisible.value = false
                                    },
                                    onCloseClick = {
                                        actionTracker.logAction(TrackerConstants.EVENT_PRO_POPUP_CLOSED)
                                        isProPopupVisible.value = false
                                    }
                                )
                            }

                            isRoomcordPopupVisible.value  && !isProPopupVisible.value -> {
                                actionTracker.logAction(TrackerConstants.EVENT_ROOMCORD_POPUP_SHOWN)
                                RoomcordPopupLayout(
                                    bottomPadding = padding.calculateBottomPadding(),
                                    onClick = {
                                        actionTracker.logAction(TrackerConstants.EVENT_ROOMCORD_POPUP_CLICKED)
                                        startActivity(Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(FulldiveConfigs.ROOMCORD_ROOM_URL_GAMES)
                                        })
                                        isRoomcordPopupVisible.value = false
                                    },
                                    onCloseClick = {
                                        actionTracker.logAction(TrackerConstants.EVENT_ROOMCORD_POPUP_CLOSED)
                                        isRoomcordPopupVisible.value = false
                                    }
                                )
                            }

                            else -> Unit
                        }
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

                    composable(MainRoute.PRO_TUTORIAL) {
                        actionTracker.logAction(TrackerConstants.EVENT_PRO_TUTORIAL_OPENED_FROM_TOOLBAR)
                        ProTutorialScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                                viewModel(
                                    factory =
                                        AdvancedSettingsViewModel.Factory(
                                            applicationContext,
                                            settingsInteractor,
                                            citra3DSKeysManager,
                                        ),
                                ),
                            navController = navController,
                            onBuyProClick = {
                                if (packageManager.isFullRoidProInstalled()) {
                                    launchApp(this@MainActivity, FulldiveConfigs.FULLROID_PRO_PACKAGE_NAME)
                                } else {
                                    openAppInGooglePlay(FulldiveConfigs.FULLROID_PRO_PACKAGE_NAME)
                                }
                            }
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
                                            citra3DSKeysManager,
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
                    composable(MainRoute.CATALOG_DETAIL) { entry ->
                        val gameId = entry.arguments?.getInt("gameId") ?: return@composable
                        CatalogDetailScreen(
                            gameId = gameId,
                            retrogradeDb = retrogradeDb,
                            onPlayClicked = { game -> gameInteractor.onGamePlay(game) },
                            onNavigateBack = { navController.popBackStack() },
                        )
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
                    onShareRoomcord = { shareRoomcordDialogDisplayed.value = it }
                )

                val game = shareRoomcordDialogDisplayed.value
                if (game != null) {
                    ShowShareDialog(
                        game = game,
                        onPositiveClicked = { sharedGame, content ->
                            shareRoomcordTextGenerator.shareGame(
                                game = sharedGame,
                                content = content,
                                onSuccess = {
                                    shareSuccessVisible.value = true
                                },
                                onError = { msg ->
                                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        onDismissRequest = { shareRoomcordDialogDisplayed.value = null }
                    )
                }

                if (shareSuccessVisible.value) {
                    ShareSuccessDialog(onDismiss = { shareSuccessVisible.value = false })
                }

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
    }

    override fun activity(): Activity = this

    override fun isBusy(): Boolean = mainViewModel.state.value.operationInProgress ?: false

    override fun showProUpgradeFor7z() {
        if (packageManager.isFullRoidProInstalled()) {
            launchApp(this, FulldiveConfigs.FULLROID_PRO_PACKAGE_NAME)
        } else {
            _navigateToProTutorial.value = true
        }
    }

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
                gameSystemHelper: GameSystemHelperImpl,
            ) = GameInteractor(
                activity,
                retrogradeDb,
                false,
                shortcutsGenerator,
                gameLauncher,
                gameSystemHelper
            )
        }
    }
}
