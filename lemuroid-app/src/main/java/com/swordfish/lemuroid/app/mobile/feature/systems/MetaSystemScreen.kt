package com.swordfish.lemuroid.app.mobile.feature.systems

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidSystemCard
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo
import com.swordfish.lemuroid.app.utils.android.compose.MergedPaddingValues

@Composable
fun MetaSystemsScreen(
    paddings: MergedPaddingValues,
    navController: NavController,
    viewModel: MetaSystemsViewModel,
) {
    val metaSystems = viewModel.availableMetaSystems.collectAsState(emptyList())
    MetaSystemsScreen(
        paddings = paddings,
        metaSystems = metaSystems.value,
        onSystemClicked = { navController.navigate("systems/${it.metaSystem.name}") }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MetaSystemsScreen(
    metaSystems: List<MetaSystemInfo>,
    onSystemClicked: (MetaSystemInfo) -> Unit,
    paddings: MergedPaddingValues
) {
    if (metaSystems.isEmpty()) {
        LemuroidEmptyView()
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(144.dp),
        contentPadding = (paddings + PaddingValues(8.dp)).asPaddingValues(),
    ) {
        items(metaSystems.size, key = { metaSystems[it].metaSystem }) { index ->
            val system = metaSystems[index]
            LemuroidSystemCard(
                modifier = Modifier.animateItemPlacement(),
                system = system,
                onClick = { onSystemClicked(system) }
            )
        }
    }
}
