package com.swordfish.lemuroid.app.utils.livedata

import androidx.lifecycle.LiveData

fun <T, K, S> LiveData<T>.combineLatest(
    other: LiveData<K>,
    combine: (data1: T, data2: K) -> S,
): LiveData<S> {
    return CombinedLiveData(this, other, combine)
}

fun <T> LiveData<T>.throttle(delayMs: Long): LiveData<T> {
    return ThrottledLiveData(this, delayMs)
}
