package com.swordfish.lemuroid.app.mobile.feature.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.shared.game.GameLauncher
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.main.BusyActivity
import com.swordfish.lemuroid.app.shared.main.GameLaunchTaskHandler
import com.swordfish.lemuroid.app.shared.settings.GamePadPreferencesHelper
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.common.coroutines.safeLaunch
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.android.RetrogradeComponentActivity
import com.swordfish.lemuroid.lib.bios.BiosManager
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import dagger.Provides
import javax.inject.Inject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import timber.log.Timber

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
        MainViewModel.Factory(applicationContext)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.safeLaunch {
            reviewManager.initialize(applicationContext)
        }

        setContent {
            val navController = rememberNavController()

            val displayProgress = mainViewModel.displayProgress.observeAsState(false)

            AppTheme {
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry.value?.destination
                val currentRoute = currentDestination?.route
                    ?.let { MainRoute.findByRoute(it) }

                LaunchedEffect(key1 = currentDestination) {
                    Timber.d("FILIPPO ${currentDestination?.displayName} ${currentDestination?.hierarchy?.map { it.route }?.toList()}")
                }

                Scaffold(
                    topBar = {
                        Surface(shadowElevation = 4.dp, tonalElevation = 4.dp) {
                            Column(Modifier.fillMaxWidth()) {
                                CenterAlignedTopAppBar(
                                    title = {
                                        Text(
                                            text = stringResource(
                                                currentRoute?.titleId ?: R.string.lemuroid_name
                                            )
                                        )
                                    },
                                    scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                                    navigationIcon = {
                                        AnimatedVisibility(
                                            visible = currentRoute?.parent != null,
                                            enter = fadeIn(),
                                            exit = fadeOut()
                                        ) {
                                            IconButton(onClick = { navController.popBackStack() }) {
                                                Icon(
                                                    Icons.Filled.ArrowBack,
                                                    "Back"
                                                ) // TODO COMPOSE FIX CONTENT DESCRIPTION
                                            }
                                        }
                                    },
                                    actions = {
                                        IconButton(onClick = { displayLemuroidHelp() }) {
                                            Icon(
                                                Icons.Outlined.Info,
                                                "Back"
                                            ) // TODO COMPOSE FIX CONTENT DESCRIPTION
                                        }
                                    },

                                    )
                                AnimatedVisibility(displayProgress.value) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            MainNavigationRoutes.values().forEach { destination ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            painter = painterResource(destination.iconId),
                                            contentDescription = stringResource(destination.titleId)
                                        )
                                    },
                                    label = { Text(stringResource(destination.titleId)) },
                                    selected = (currentRoute?.parent
                                        ?: currentRoute) == destination.route.startDestination,
                                    onClick = {
                                        navController.navigate(destination.route.route) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            // on the back stack as users select items
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = false
                                            }
                                            // Avoid multiple copies of the same destination when
                                            // reselecting the same item
                                            launchSingleTop = true
                                            // Restore state when reselecting a previously selected item
                                            restoreState = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController,
                        startDestination = MainRoute.HOME.route,
                        Modifier.padding(padding)
                    ) {
                        composable(MainRoute.HOME) {
                            HomeScreen(
                                viewModel(
                                    factory = HomeViewModel.Factory(
                                        applicationContext,
                                        retrogradeDb,
                                        settingsInteractor
                                    )
                                ),
                                gameInteractor
                            )
                        }
                        composable(MainRoute.FAVORITES) {
                            FavoritesScreen(
                                viewModel(
                                    factory = FavoritesViewModel.Factory(retrogradeDb)
                                ),
                                gameInteractor = gameInteractor
                            )
                        }
                        composable(MainRoute.SEARCH) {
                            SearchScreen(
                                viewModel(
                                    factory = SearchViewModel.Factory(retrogradeDb)
                                ),
                                gameInteractor = gameInteractor
                            )
                        }
                        navigation(MainGraph.SYSTEMS) {
                            composable(MainRoute.SYSTEMS) {
                                MetaSystemsScreen(
                                    navController,
                                    viewModel(
                                        factory = MetaSystemsViewModel.Factory(
                                            retrogradeDb,
                                            applicationContext
                                        )
                                    )
                                )
                            }
                            composable(MainRoute.SYSTEM_GAMES) { entry ->
                                val metaSystemId = entry.arguments?.getString("metaSystemId")
                                GamesScreen(
                                    viewModel = viewModel(
                                        factory = GamesViewModel.Factory(
                                            retrogradeDb,
                                            MetaSystemID.valueOf(metaSystemId!!)
                                        )
                                    ),
                                    gameInteractor
                                )
                            }
                        }
                        navigation(MainGraph.SETTINGS) {
                            composable(MainRoute.SETTINGS) {
                                SettingsScreen(
                                    viewModel = viewModel(
                                        factory = SettingsViewModel.Factory(
                                            applicationContext,
                                            settingsInteractor,
                                            saveSyncManager,
                                            FlowSharedPreferences(
                                                SharedPreferencesHelper.getLegacySharedPreferences(
                                                    applicationContext
                                                )
                                            )
                                        )
                                    ),
                                    navController = navController
                                )
                            }
                            composable(MainRoute.SETTINGS_ADVANCED) {
                                AdvancedSettingsScreen(
                                    viewModel = viewModel(
                                        factory = AdvancedSettingsViewModel.Factory(
                                            applicationContext,
                                            settingsInteractor
                                        )
                                    ),
                                    navController
                                )
                            }
                            composable(MainRoute.SETTINGS_BIOS) {
                                BiosScreen(
                                    viewModel = viewModel(
                                        factory = BiosSettingsViewModel.Factory(biosManager)
                                    )
                                )
                            }
                            composable(MainRoute.SETTINGS_CORES_SELECTION) {
                                CoresSelectionScreen(
                                    viewModel = viewModel(
                                        factory = CoresSelectionViewModel.Factory(
                                            applicationContext,
                                            coresSelection
                                        )
                                    )
                                )
                            }
                            composable(MainRoute.SETTINGS_INPUT_DEVICES) {
                                InputDevicesSettingsScreen(
                                    viewModel = viewModel(
                                        factory = InputDevicesSettingsViewModel.Factory(
                                            applicationContext,
                                            inputDeviceManager
                                        )
                                    )
                                )
                            }
                            composable(MainRoute.SETTINGS_SAVE_SYNC) {
                                SaveSyncSettingsScreen(
                                    viewModel = viewModel(
                                        factory = SaveSyncSettingsViewModel.Factory(
                                            application,
                                            saveSyncManager
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun activity(): Activity = this
    override fun isBusy(): Boolean = mainViewModel.displayProgress.value ?: false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            BaseGameActivity.REQUEST_PLAY_GAME -> {
                GlobalScope.safeLaunch {
                    gameLaunchTaskHandler.handleGameFinish(
                        true,
                        this@MainActivity,
                        resultCode,
                        data
                    )
                }
            }
        }
    }

    private fun displayLemuroidHelp() {
        val systemFolders = SystemID.values()
            .joinToString(", ") { "<i>${it.dbname}</i>" }

        val message = getString(R.string.lemuroid_help_content).replace("\$SYSTEMS", systemFolders)
        MaterialAlertDialogBuilder(this)
            .setMessage(Html.fromHtml(message))
            .show()
    }

    @dagger.Module
    abstract class Module {

        @dagger.Module
        companion object {

            @Provides
            @PerActivity
            @JvmStatic
            fun settingsInteractor(activity: MainActivity, directoriesManager: DirectoriesManager) =
                SettingsInteractor(activity, directoriesManager)

            @Provides
            @PerActivity
            @JvmStatic
            fun gamePadPreferencesHelper(inputDeviceManager: InputDeviceManager) =
                GamePadPreferencesHelper(inputDeviceManager, false)

            @Provides
            @PerActivity
            @JvmStatic
            fun gameInteractor(
                activity: MainActivity,
                retrogradeDb: RetrogradeDatabase,
                shortcutsGenerator: ShortcutsGenerator,
                gameLauncher: GameLauncher
            ) =
                GameInteractor(activity, retrogradeDb, false, shortcutsGenerator, gameLauncher)
        }
    }
}
