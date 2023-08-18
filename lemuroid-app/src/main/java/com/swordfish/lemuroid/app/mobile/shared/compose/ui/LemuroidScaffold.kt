package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.app.mobile.feature.main.MainViewModel

@Composable
fun LemuroidScaffold(
    route: MainRoute,
    navController: NavController,
    mainUIState: MainViewModel.UiState,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = { LemuroidTopAppBar(route, navController, mainUIState) },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
