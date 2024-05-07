package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    navController: NavHostController,
    onHelpPressed: () -> Unit,
    mainUIState: MainViewModel.UiState,
    onUpdateQueryString: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1f),
        ) {
            // Standard TextField has huge margins that can't be customized.
            // We set the background to invisible and draw a surface behind
            Surface(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                shape = RoundedCornerShape(100),
                tonalElevation = 16.dp,
            ) { }
            TextField(
                value = mainUIState.searchQuery,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, end = 8.dp)
                        .focusRequester(focusRequester),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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

        val context = LocalContext.current
        val colors = TopAppBarDefaults.topAppBarColors()
        CompositionLocalProvider(LocalContentColor provides colors.actionIconContentColor) {
            Box(modifier = Modifier.padding(4.dp)) {
                LemuroidTopBarActions(
                    MainRoute.SEARCH,
                    navController,
                    onHelpPressed = onHelpPressed,
                    context = context,
                    saveSyncEnabled = mainUIState.saveSyncEnabled,
                    operationsInProgress = mainUIState.operationInProgress,
                )
            }
        }
    }
}
