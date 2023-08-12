package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.swordfish.lemuroid.R

import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.composable(
    route: MainRoute,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    this.composable(route = route.route, arguments = route.arguments, content = content)
}

fun NavGraphBuilder.navigation(route: MainGraph, builder: NavGraphBuilder.() -> Unit) {
    this.navigation(route.startDestination.route, route.route, builder)
}

fun NavController.navigateToRoute(route: MainRoute) {
    this.navigate(route.route)
}

// TODO COMPOSE... Navigation actually have to depend on a better model that we define here...
enum class MainRoute(
    val route: String,
    @StringRes val titleId: Int,
    val parent: MainRoute? = null,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    HOME(
        route = "home",
        titleId = R.string.title_home
    ),
    FAVORITES(
        route = "favorites",
        titleId = R.string.favorites
    ),
    SEARCH(
        route = "search",
        titleId = R.string.title_search
    ),
    SYSTEMS(
        route = "systems/home",
        titleId = R.string.title_systems
    ),
    SYSTEM_GAMES(
        route = "systems/{metaSystemId}",
        titleId = R.string.title_games,
        parent = SYSTEMS,
        listOf(navArgument("metaSystemId") { type = NavType.StringType })
    ),
    SETTINGS(
        route = "settings/home",
        titleId = R.string.title_settings,
        parent = SETTINGS
    ),
    SETTINGS_ADVANCED(
        route = "settings/advanced",
        titleId = R.string.settings_title_advanced_settings,
        parent = SETTINGS
    ),
    SETTINGS_BIOS(
        route = "settings/bios",
        titleId = R.string.settings_title_display_bios_info,
        parent = SETTINGS
    ),
    SETTINGS_CORES_SELECTION(
        route = "settings/cores",
        titleId = R.string.settings_title_open_cores_selection,
        parent = SETTINGS
    ),
    SETTINGS_INPUT_DEVICES(
        route = "settings/inputdevices",
        titleId = R.string.settings_title_gamepad_settings,
        parent = SETTINGS
    ),
    SETTINGS_SAVE_SYNC(
        route = "settings/savesync",
        titleId = R.string.settings_title_save_sync,
        parent = SETTINGS
    );

    companion object {
        fun findByRoute(route: String): MainRoute {
            return values().first { it.route == route }
        }
    }
}

enum class MainGraph(val route: String, val startDestination: MainRoute) {
    HOME("home", MainRoute.HOME),
    FAVORITES("favorites", MainRoute.FAVORITES),
    SEARCH("search", MainRoute.SEARCH),
    SYSTEMS("systems", MainRoute.SYSTEMS),
    SETTINGS("settings", MainRoute.SETTINGS),
}

enum class MainNavigationRoutes(val route: MainGraph, @StringRes val titleId: Int, @DrawableRes val iconId: Int) {
    HOME(MainGraph.HOME, R.string.title_home, R.drawable.ic_home_black_24dp),
    FAVORITES(MainGraph.FAVORITES, R.string.favorites, R.drawable.ic_favorite_black_24dp),
    SEARCH(MainGraph.SEARCH, R.string.title_search, R.drawable.ic_search_black_24dp),
    SYSTEMS(MainGraph.SYSTEMS, R.string.title_systems, R.drawable.ic_videogame_asset_black_24dp),
    SETTINGS(MainGraph.SETTINGS, R.string.title_settings, R.drawable.ic_settings_black_24dp),
}
