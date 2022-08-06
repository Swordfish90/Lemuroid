package com.swordfish.lemuroid.common.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import timber.log.Timber

suspend fun <T> Flow<T>.safeCollect(block: suspend (T) -> Unit) {
    this.safeCollect(Timber::e, block)
}

suspend fun <T> Flow<T>.safeCollect(onError: suspend (e: Throwable) -> Unit, block: suspend (T) -> Unit) {
    this.catch { Timber.e(it) }
        .collect {
            try {
                block(it)
            } catch (e: Throwable) {
                onError(e)
            }
        }
}

// TODO COROUTINES Split into multiple functions...
fun <T> Flow<T>.batch(maxSize: Int, maxMillis: Int): Flow<List<T>> = flow {
    val batch = mutableListOf<T>()
    var lastEmission = System.currentTimeMillis()

    collect { value ->
        batch.add(value)
        if (batch.size >= maxSize || System.currentTimeMillis() > lastEmission + maxMillis) {
            emit(batch.toList())
            batch.clear()
            lastEmission = System.currentTimeMillis()
        }
    }

    if (batch.isNotEmpty()) emit(batch)
}
