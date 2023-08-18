package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import android.content.Context
import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.lib.library.SystemID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LemuroidTopAppBar(
    route: MainRoute,
    navController: NavController,
    displayProgress: Boolean,
) {
    val context = LocalContext.current
    LemuroidTopAppBarContainer(displayProgress) {
        TopAppBar(
            title = { Text(text = stringResource(route.titleId)) },
            navigationIcon = {
                AnimatedVisibility(
                    visible = route.parent != null,
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
                LemuroidTopBarActions(route, navController, context)
            },
        )
    }
}

@Composable
fun LemuroidTopBarActions(route: MainRoute, navController: NavController, context: Context) {
    IconButton(
        onClick = { displayLemuroidHelp(context) }
    ) {
        Icon(
            Icons.Outlined.Info,
            "Back"
        ) // TODO COMPOSE FIX CONTENT DESCRIPTION
    }
    if (route.showBottomNavigation) {
        IconButton(
            onClick = { navController.navigate(MainRoute.SETTINGS.route) }
        ) {
            Icon(
                Icons.Outlined.Settings,
                "Settings"
            ) // TODO COMPOSE FIX CONTENT DESCRIPTION
        }
    }
}

@Composable
fun LemuroidTopAppBarContainer(displayProgress: Boolean, content: @Composable () -> Unit) {
    Column {
        Surface(tonalElevation = 2.dp, shadowElevation = 2.dp) {
            content()
        }

        AnimatedVisibility(displayProgress) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

private fun displayLemuroidHelp(context: Context) {
    val systemFolders = SystemID.values()
        .joinToString(", ") { "<i>${it.dbname}</i>" }

    val message = context.getString(R.string.lemuroid_help_content).replace("\$SYSTEMS", systemFolders)
    MaterialAlertDialogBuilder(context)
        .setMessage(Html.fromHtml(message))
        .show()
}