package com.swordfish.lemuroid.app.mobile.feature.main

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork

@Composable
fun MainTopBar(
    currentRoute: MainRoute,
    navController: NavHostController,
    onHelpPressed: () -> Unit,
    onUpdateQueryString: (String) -> Unit,
    mainUIState: MainViewModel.UiState,
    titleOverride: String? = null,
    onScrapePlatformPressed: (() -> Unit)? = null,
    onScrapeAllPlatformsPressed: (() -> Unit)? = null,
    onCancelScrapePlatform: (() -> Unit)? = null,
    isSystemSearchActive: Boolean = false,
    systemSearchQuery: String = "",
    onSystemSearchToggle: ((Boolean) -> Unit)? = null,
    onSystemSearchQueryChange: ((String) -> Unit)? = null,
    isFavoritesSearchActive: Boolean = false,
    favoritesSearchQuery: String = "",
    onFavoritesSearchToggle: ((Boolean) -> Unit)? = null,
    onFavoritesSearchQueryChange: ((String) -> Unit)? = null,
    onSortPressed: (() -> Unit)? = null,
    isGridView: Boolean = false,
    onToggleViewMode: (() -> Unit)? = null,
) {
    Column {
        LemuroidTopAppBar(
            route = currentRoute,
            navController = navController,
            mainUIState = mainUIState,
            titleOverride = titleOverride,
            onHelpPressed = onHelpPressed,
            onUpdateQueryString = onUpdateQueryString,
            onScrapePlatformPressed = onScrapePlatformPressed,
            onScrapeAllPlatformsPressed = onScrapeAllPlatformsPressed,
            onCancelScrapePlatform = onCancelScrapePlatform,
            isSystemSearchActive = isSystemSearchActive,
            systemSearchQuery = systemSearchQuery,
            onSystemSearchToggle = onSystemSearchToggle,
            onSystemSearchQueryChange = onSystemSearchQueryChange,
            isFavoritesSearchActive = isFavoritesSearchActive,
            favoritesSearchQuery = favoritesSearchQuery,
            onFavoritesSearchToggle = onFavoritesSearchToggle,
            onFavoritesSearchQueryChange = onFavoritesSearchQueryChange,
            onSortPressed = onSortPressed,
            isGridView = isGridView,
            onToggleViewMode = onToggleViewMode,
        )

        AnimatedVisibility(mainUIState.operationInProgress) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LemuroidTopAppBar(
    route: MainRoute,
    navController: NavController,
    mainUIState: MainViewModel.UiState,
    titleOverride: String? = null,
    onHelpPressed: () -> Unit,
    onUpdateQueryString: (String) -> Unit,
    onScrapePlatformPressed: (() -> Unit)? = null,
    onScrapeAllPlatformsPressed: (() -> Unit)? = null,
    onCancelScrapePlatform: (() -> Unit)? = null,
    isSystemSearchActive: Boolean = false,
    systemSearchQuery: String = "",
    onSystemSearchToggle: ((Boolean) -> Unit)? = null,
    onSystemSearchQueryChange: ((String) -> Unit)? = null,
    isFavoritesSearchActive: Boolean = false,
    favoritesSearchQuery: String = "",
    onFavoritesSearchToggle: ((Boolean) -> Unit)? = null,
    onFavoritesSearchQueryChange: ((String) -> Unit)? = null,
    onSortPressed: (() -> Unit)? = null,
    isGridView: Boolean = false,
    onToggleViewMode: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val topBarColor = BottomAppBarDefaults.containerColor

    TopAppBar(
        title = {
            if (route == MainRoute.SEARCH) {
                LemuroidSearchView(
                    query = mainUIState.searchQuery,
                    placeholder = stringResource(R.string.search_hint_games),
                    onUpdateQueryString = onUpdateQueryString,
                    onClose = {
                        if (mainUIState.searchQuery.isNotEmpty()) {
                            onUpdateQueryString("")
                        } else {
                            navController.popBackStack()
                        }
                    },
                )
            } else if (route == MainRoute.SYSTEM_GAMES && isSystemSearchActive) {
                LemuroidSearchView(
                    query = systemSearchQuery,
                    placeholder = stringResource(R.string.search_hint_game_or_genre),
                    onUpdateQueryString = { onSystemSearchQueryChange?.invoke(it) },
                    onClose = {
                        if (systemSearchQuery.isNotEmpty()) {
                            onSystemSearchQueryChange?.invoke("")
                        } else {
                            onSystemSearchToggle?.invoke(false)
                        }
                    },
                )
            } else if (route == MainRoute.FAVORITES && isFavoritesSearchActive) {
                LemuroidSearchView(
                    query = favoritesSearchQuery,
                    placeholder = stringResource(R.string.search_hint_game_or_genre),
                    onUpdateQueryString = { onFavoritesSearchQueryChange?.invoke(it) },
                    onClose = {
                        if (favoritesSearchQuery.isNotEmpty()) {
                            onFavoritesSearchQueryChange?.invoke("")
                        } else {
                            onFavoritesSearchToggle?.invoke(false)
                        }
                    },
                )
            } else {
                Text(
                    text = titleOverride ?: stringResource(route.titleId),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                scrolledContainerColor = topBarColor,
                containerColor = topBarColor,
            ),
        navigationIcon = {
            AnimatedVisibility(
                visible = route.parent != null || (route == MainRoute.FAVORITES && isFavoritesSearchActive),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                IconButton(
                    onClick = {
                        if (route == MainRoute.SYSTEM_GAMES && isSystemSearchActive) {
                            onSystemSearchQueryChange?.invoke("")
                            onSystemSearchToggle?.invoke(false)
                        } else if (route == MainRoute.FAVORITES && isFavoritesSearchActive) {
                            onFavoritesSearchQueryChange?.invoke("")
                            onFavoritesSearchToggle?.invoke(false)
                        } else {
                            navController.popBackStack()
                        }
                    },
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        stringResource(id = R.string.back),
                    )
                }
            }
        },
        actions = {
            LemuroidTopBarActions(
                route = route,
                navController = navController,
                context = context,
                saveSyncEnabled = mainUIState.saveSyncEnabled,
                onHelpPressed = onHelpPressed,
                operationsInProgress = mainUIState.operationInProgress,
                onScrapePlatformPressed = onScrapePlatformPressed,
                onScrapeAllPlatformsPressed = onScrapeAllPlatformsPressed,
                onCancelScrapePlatform = onCancelScrapePlatform,
                isSystemSearchActive = isSystemSearchActive,
                onSystemSearchToggle = onSystemSearchToggle,
                isFavoritesSearchActive = isFavoritesSearchActive,
                onFavoritesSearchToggle = onFavoritesSearchToggle,
                onSortPressed = onSortPressed,
                isGridView = isGridView,
                onToggleViewMode = onToggleViewMode,
            )
        },
    )
}

@Composable
fun LemuroidTopBarActions(
    route: MainRoute,
    navController: NavController,
    context: Context,
    saveSyncEnabled: Boolean,
    operationsInProgress: Boolean,
    onHelpPressed: () -> Unit,
    onScrapePlatformPressed: (() -> Unit)? = null,
    onScrapeAllPlatformsPressed: (() -> Unit)? = null,
    onCancelScrapePlatform: (() -> Unit)? = null,
    isSystemSearchActive: Boolean = false,
    onSystemSearchToggle: ((Boolean) -> Unit)? = null,
    isFavoritesSearchActive: Boolean = false,
    onFavoritesSearchToggle: ((Boolean) -> Unit)? = null,
    onSortPressed: (() -> Unit)? = null,
    isGridView: Boolean = false,
    onToggleViewMode: (() -> Unit)? = null,
) {
    Row {
        if (route == MainRoute.SYSTEM_GAMES) {
            if (!isSystemSearchActive) {
                IconButton(
                    onClick = { onSystemSearchToggle?.invoke(true) },
                ) {
                    Icon(
                        Icons.Filled.Search,
                        stringResource(R.string.title_search),
                    )
                }
            }
            IconButton(
                onClick = {
                    if (operationsInProgress) {
                        onCancelScrapePlatform?.invoke()
                    } else {
                        onScrapePlatformPressed?.invoke()
                    }
                },
            ) {
                if (operationsInProgress) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(24.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                        )
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cancel),
                            modifier = Modifier.size(12.dp),
                        )
                    }
                } else {
                    Icon(
                        Icons.Filled.Refresh,
                        stringResource(R.string.scrape_platform_games),
                    )
                }
            }
        } else if (route == MainRoute.HOME) {
            IconButton(
                onClick = {
                    navController.navigate(MainRoute.SEARCH.route) {
                        popUpTo(MainRoute.HOME.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            ) {
                Icon(
                    Icons.Filled.Search,
                    stringResource(R.string.title_search),
                )
            }
        } else if (route == MainRoute.SYSTEMS) {
            IconButton(
                onClick = {
                    navController.navigate(MainRoute.SEARCH.route) {
                        popUpTo(MainRoute.HOME.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            ) {
                Icon(
                    Icons.Filled.Search,
                    stringResource(R.string.title_search),
                )
            }
            IconButton(
                onClick = {
                    if (operationsInProgress) {
                        onCancelScrapePlatform?.invoke()
                    } else {
                        onScrapeAllPlatformsPressed?.invoke()
                    }
                },
            ) {
                if (operationsInProgress) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(24.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                        )
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cancel),
                            modifier = Modifier.size(12.dp),
                        )
                    }
                } else {
                    Icon(
                        Icons.Filled.Refresh,
                        stringResource(R.string.scrape_all_platforms),
                    )
                }
            }
        } else if (route == MainRoute.FAVORITES) {
            if (!isFavoritesSearchActive) {
                IconButton(
                    onClick = { onFavoritesSearchToggle?.invoke(true) },
                ) {
                    Icon(
                        Icons.Filled.Search,
                        stringResource(R.string.title_search),
                    )
                }
            }
        } else if (route != MainRoute.SEARCH) {
            IconButton(
                onClick = { onHelpPressed() },
            ) {
                Icon(
                    Icons.Outlined.Info,
                    stringResource(R.string.mobile_settings_help),
                )
            }
        }
        if (saveSyncEnabled) {
            IconButton(
                onClick = { SaveSyncWork.enqueueManualWork(context.applicationContext) },
                enabled = !operationsInProgress,
            ) {
                Icon(
                    Icons.Outlined.CloudSync,
                    stringResource(R.string.save_sync),
                )
            }
        }
        if (route == MainRoute.SYSTEM_GAMES || route == MainRoute.FAVORITES) {
            var showOverflowMenu by remember { mutableStateOf(false) }

            Box {
                IconButton(onClick = { showOverflowMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.settings),
                    )
                }
                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false },
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(
                                    if (isGridView) R.string.menu_list_view else R.string.menu_grid_view,
                                ),
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isGridView) {
                                    Icons.AutoMirrored.Outlined.ViewList
                                } else {
                                    Icons.Outlined.GridView
                                },
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            onToggleViewMode?.invoke()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_sort_by)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            onSortPressed?.invoke()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            navController.navigate(MainRoute.SETTINGS.route)
                        },
                    )
                }
            }
        } else if (route.showBottomNavigation) {
            IconButton(
                onClick = { navController.navigate(MainRoute.SETTINGS.route) },
            ) {
                Icon(
                    Icons.Outlined.Settings,
                    stringResource(R.string.settings),
                )
            }
        }
    }
}

@Composable
private fun LemuroidSearchView(
    query: String,
    placeholder: String,
    onUpdateQueryString: (String) -> Unit,
    onClose: (() -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, bottom = 8.dp, end = 4.dp),
            shape = RoundedCornerShape(100),
            tonalElevation = 16.dp,
        ) { }

        TextField(
            value = query,
            modifier =
                Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyMedium,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            trailingIcon = if (onClose != null) {
                {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cancel),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            } else {
                null
            },
            onValueChange = { onUpdateQueryString(it) },
            singleLine = true,
            keyboardActions =
                KeyboardActions(
                    onDone = { focusManager.clearFocus(true) },
                ),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
        )
    }
}
