package com.swordfish.lemuroid.app.mobile.feature.main

import android.net.Uri
import kotlinx.serialization.json.Json
import com.swordfish.lemuroid.lib.library.db.entity.Game
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.swordfish.lemuroid.R

fun NavGraphBuilder.composable(
    route: MainRoute,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    this.composable(route = route.route, arguments = route.arguments, content = content)
}

fun NavGraphBuilder.dialog(
    route: MainRoute,
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    dialog(
        route = route.route,
        arguments = route.arguments,
        dialogProperties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        content = content,
    )
}

fun NavController.navigateToEditGame(game: Game) {
    val argument = Uri.encode(Json.encodeToString(Game.serializer(), game))
    navigate(MainRoute.EDIT_GAME.route.replace("{game}", argument))
}

fun NavController.navigateToRoute(route: MainRoute) {
    this.navigate(route.route)
}

enum class MainRoute(
    val route: String,
    @StringRes val titleId: Int,
    val parent: MainRoute? = null,
    val arguments: List<NamedNavArgument> = emptyList(),
) {
    HOME(
        route = "home",
        titleId = R.string.title_home,
    ),
    FAVORITES(
        route = "favorites",
        titleId = R.string.favorites,
    ),
    SEARCH(
        route = "search",
        titleId = R.string.title_search,
    ),
    SYSTEMS(
        route = "systems/home",
        titleId = R.string.title_systems,
    ),
    SYSTEM_GAMES(
        route = "systems/{metaSystemId}",
        titleId = R.string.title_games,
        parent = SYSTEMS,
        listOf(navArgument("metaSystemId") { type = NavType.StringType }),
    ),
    EDIT_GAME(
        route = "games/edit/{game}",
        titleId = R.string.game_context_menu_edit,
        parent = HOME,
        arguments = listOf(navArgument("game") { type = NavType.StringType }),
    ),
    SETTINGS(
        route = "settings/home",
        titleId = R.string.title_settings,
    ),
    SETTINGS_ADVANCED(
        route = "settings/advanced",
        titleId = R.string.settings_title_advanced_settings,
        parent = SETTINGS,
    ),
    SETTINGS_BIOS(
        route = "settings/bios",
        titleId = R.string.settings_title_display_bios_info,
        parent = SETTINGS,
    ),
    SETTINGS_CORES_SELECTION(
        route = "settings/cores",
        titleId = R.string.settings_title_open_cores_selection,
        parent = SETTINGS,
    ),
    SETTINGS_INPUT_DEVICES(
        route = "settings/inputdevices",
        titleId = R.string.settings_title_gamepad_settings,
        parent = SETTINGS,
    ),
    SETTINGS_SAVE_SYNC(
        route = "settings/savesync",
        titleId = R.string.settings_title_save_sync,
        parent = SETTINGS,
    ),
    ;

    val root = root()

    private fun root(): MainRoute {
        return parent?.root() ?: this
    }

    companion object {
        fun findByRoute(route: String): MainRoute {
            return values().first { it.route == route }
        }
    }
}

enum class MainNavigationRoutes(
    val route: MainRoute,
    @StringRes val titleId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(MainRoute.HOME, R.string.title_home, Icons.Filled.Home, Icons.Outlined.Home),
    FAVORITES(MainRoute.FAVORITES, R.string.favorites, Icons.Filled.Favorite, Icons.Filled.FavoriteBorder),
    SYSTEMS(MainRoute.SYSTEMS, R.string.title_systems, Icons.Filled.VideogameAsset, Icons.Outlined.VideogameAsset),
    SEARCH(MainRoute.SEARCH, R.string.title_search, Icons.Filled.Search, Icons.Outlined.Search),
    SETTINGS(MainRoute.SETTINGS, R.string.title_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
}
