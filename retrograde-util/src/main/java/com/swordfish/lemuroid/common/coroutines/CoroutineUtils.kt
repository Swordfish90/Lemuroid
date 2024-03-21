package com.swordfish.lemuroid.common.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.times

fun CoroutineScope.safeLaunch(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit,
) {
    launch(context) {
        try {
            block()
        } catch (e: Throwable) {
            Timber.e(e)
        }
    }
}

suspend fun <T> retry(
    attempts: Int,
    delay: Duration,
    block: suspend (Int) -> T,
): Result<T> {
    assert(attempts >= 1)

    val lastAttempt = attempts - 1
    for (attempt in 0 until lastAttempt) {
        val result = runCatching { block(attempt) }
        if (result.isSuccess) {
            return result
        }
        delay((attempt + 1) * delay)
    }

    return runCatching { block(lastAttempt) }
}
