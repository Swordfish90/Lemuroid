package com.swordfish.lemuroid.common.coroutines

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun Fragment.launchOnState(
    state: Lifecycle.State,
    block: suspend () -> Unit,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(state) {
            block()
        }
    }
}

fun LifecycleOwner.launchOnState(
    state: Lifecycle.State,
    block: suspend () -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            block()
        }
    }
}

fun LifecycleOwner.launchOnState(
    context: CoroutineContext = EmptyCoroutineContext,
    state: Lifecycle.State,
    block: suspend () -> Unit,
) {
    lifecycleScope.launch(context) {
        repeatOnLifecycle(state) {
            block()
        }
    }
}
