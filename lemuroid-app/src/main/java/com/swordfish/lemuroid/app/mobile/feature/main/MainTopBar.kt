package com.swordfish.lemuroid.app.mobile.feature.main

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
) {
    Column {
        Surface(tonalElevation = BottomAppBarDefaults.ContainerElevation) {
            Crossfade(
                targetState = currentRoute,
                label = "MainTopBar"
            ) { route ->
                when (route) {
                    MainRoute.SEARCH -> SearchTopBar(
                        navController = navController,
                        onHelpPressed = onHelpPressed,
                        mainUIState = mainUIState,
                        onUpdateQueryString = onUpdateQueryString,
                    )
                    else -> LemuroidTopAppBar(route, navController, mainUIState, onHelpPressed)
                }
            }
        }

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
    onHelpPressed: () -> Unit,
) {
    val context = LocalContext.current
    val topBarColor =
        MaterialTheme.colorScheme.surfaceColorAtElevation(
            BottomAppBarDefaults.ContainerElevation,
        )

    TopAppBar(
        title = { Text(text = stringResource(route.titleId)) },
        colors =
        TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = topBarColor,
            containerColor = topBarColor,
        ),
        navigationIcon = {
            AnimatedVisibility(
                visible = route?.parent != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
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
) {
    Row {
        IconButton(
            onClick = { onHelpPressed() },
        ) {
            Icon(
                Icons.Outlined.Info,
                stringResource(R.string.mobile_settings_help),
            )
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
        if (route.showBottomNavigation) {
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
