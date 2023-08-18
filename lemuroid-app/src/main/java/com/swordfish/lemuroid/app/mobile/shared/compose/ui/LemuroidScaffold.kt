package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute

@Composable
fun LemuroidScaffold(
    route: MainRoute,
    navController: NavController,
    displayProgress: Boolean,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = { LemuroidTopAppBar(route, navController, displayProgress) },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
