package com.swordfish.lemuroid.app.utils.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.Transformations
import io.reactivex.Observable

fun <T, K, S> LiveData<T>.combineLatest(
    other: LiveData<K>,
    combine: (data1: T, data2: K) -> S
): LiveData<S> {
    return CombinedLiveData(this, other, combine)
}

fun <T> LiveData<T>.throttle(delayMs: Long): LiveData<T> {
    return ThrottledLiveData(this, delayMs)
}

fun <T, K> LiveData<T>.map(mapper: (T) -> K): LiveData<K> {
    return Transformations.map(this, mapper)
}

fun <T> LiveData<T>.toObservable(lifecycleOwner: LifecycleOwner): Observable<T> {
    return LiveDataReactiveStreams.toPublisher(lifecycleOwner, this)
        .let { Observable.fromPublisher(it) }
}
