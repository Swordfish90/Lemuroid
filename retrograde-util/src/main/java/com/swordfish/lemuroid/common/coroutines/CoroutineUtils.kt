package com.swordfish.lemuroid.common.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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
