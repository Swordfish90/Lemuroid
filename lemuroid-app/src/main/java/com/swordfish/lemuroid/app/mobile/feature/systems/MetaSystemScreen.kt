package com.swordfish.lemuroid.app.mobile.feature.systems

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidSystemCard
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo

@Composable
fun MetaSystemsScreen(
    navController: NavController,
    viewModel: MetaSystemsViewModel,
) {
    val metaSystems = viewModel.availableMetaSystems.collectAsState(emptyList())
    MetaSystemsScreen(
        metaSystems = metaSystems.value,
        onSystemClicked = { navController.navigate("systems/${it.metaSystem.name}") }
    )
}

@Composable
fun MetaSystemsScreen(
    metaSystems: List<MetaSystemInfo>,
    onSystemClicked: (MetaSystemInfo) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(144.dp),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(metaSystems.size, key = { metaSystems[it].metaSystem }) { index ->
            val system = metaSystems[index]
            LemuroidSystemCard(system) { onSystemClicked(system) }
        }
    }
}
