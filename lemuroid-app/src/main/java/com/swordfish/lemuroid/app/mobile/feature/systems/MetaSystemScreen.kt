package com.swordfish.lemuroid.app.mobile.feature.systems

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.SystemCard
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo

@Composable
fun MetaSystemsScreen(
    metaSystems: List<MetaSystemInfo>,
    onSystemClicked: (MetaSystemInfo) -> Unit
) {
    AppTheme {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(144.dp),
            contentPadding = PaddingValues(8.dp),
        ) {
            items(metaSystems.size, key = { metaSystems[it].metaSystem }) { index ->
                val system = metaSystems[index]
                SystemCard(system) { onSystemClicked(system) }
            }
        }
    }
}
