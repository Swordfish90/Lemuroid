package com.swordfish.lemuroid.common.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> Flow<T>.batch(maxSize: Int, maxSeconds: Int): Flow<List<T>> = flow {
    val batch = mutableListOf<T>()
    val maxMillis = maxSeconds * 1000L
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
