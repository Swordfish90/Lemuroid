package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavController
import com.swordfish.lemuroid.app.mobile.feature.main.MainRoute
import com.swordfish.lemuroid.app.mobile.feature.main.MainViewModel
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LemuroidScaffold(
    route: MainRoute,
    navController: NavController,
    mainUIState: MainViewModel.UiState,
    outerPadding: PaddingValues,
    content: @Composable (MergedPaddingValues) -> Unit
) {
    // TODO COMPOSE... This currently looks like a parallax animation. Let's see if we can improve it
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        rememberTopAppBarState()
    )
    Scaffold(
        topBar = { LemuroidTopAppBar(route, navController, mainUIState, scrollBehavior) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
