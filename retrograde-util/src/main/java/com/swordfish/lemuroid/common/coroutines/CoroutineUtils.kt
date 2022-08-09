package com.swordfish.lemuroid.common.coroutines

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

fun CoroutineScope.safeLaunch(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    launch(context) {
        try {
            block()
        } catch (e: Throwable) {
            Timber.e(e)
        }
    }
}
