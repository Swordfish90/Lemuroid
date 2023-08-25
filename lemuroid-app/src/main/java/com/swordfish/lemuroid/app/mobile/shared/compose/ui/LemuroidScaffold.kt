package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.app.mobile.feature.main.MainViewModel
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues

@Composable
fun LemuroidScaffold(
    route: MainRoute,
    navController: NavController,
    mainUIState: MainViewModel.UiState,
    outerPadding: PaddingValues,
    content: @Composable (MergedPaddingValues) -> Unit
) {
    Scaffold(
        topBar = { LemuroidTopAppBar(route, navController, mainUIState) },
    ) { innerPadding ->

        val paddings = mutableListOf<PaddingValues>().apply {
            add(innerPadding)

            if (route.showBottomNavigation) {
                add(outerPadding)
            }
        }

        content(MergedPaddingValues(paddings))
    }
}
