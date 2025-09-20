package com.swordfish.lemuroid.app.utils.android

import androidx.annotation.ArrayRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
@ReadOnlyComposable
fun stringListResource(
    @ArrayRes id: Int,
): List<String> {
    return LocalContext.current.resources
        .getStringArray(id)
        .toList()
}

@Composable
fun ComposableLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit,
) {
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { source, event ->
                onEvent(source, event)
            }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
