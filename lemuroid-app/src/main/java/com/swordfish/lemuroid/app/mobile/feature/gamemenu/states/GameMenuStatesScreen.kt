package com.swordfish.lemuroid.app.mobile.feature.gamemenu.states

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink

@Composable
fun GameMenuStatesScreen(
    viewModel: GameMenuStatesViewModel,
    onStateClicked: (Int) -> Unit,
) {
    val state = viewModel.uiStates.collectAsState(initial = GameMenuStatesViewModel.State())

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        state.value.entries.forEachIndexed { index, entry ->
            LemuroidSettingsMenuLink(
                title = { Text(text = entry.title) },
                subtitle = { Text(text = entry.description) },
                enabled = entry.enabled,
                icon = {
                    if (entry.preview != null) {
                        Image(
                            modifier = Modifier.size(48.dp),
                            bitmap = entry.preview.asImageBitmap(),
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                        )
                    }
                },
                onClick = { onStateClicked(index) },
            )
        }
    }
}
