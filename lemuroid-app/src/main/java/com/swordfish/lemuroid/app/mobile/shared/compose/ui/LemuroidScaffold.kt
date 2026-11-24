package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LemuroidScaffold(
    loading: Boolean,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(modifier = modifier, topBar = topBar) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            content(padding)
            LemuroidOperationProgressIndicator(
                visible = loading,
                modifier = Modifier.padding(
                    top = maxOf(
                        padding.calculateTopPadding(),
                        WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    ),
                ),
            )
        }
    }
}
