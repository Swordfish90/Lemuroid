package com.swordfish.lemuroid.app.utils.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

class CombinedLiveData<T, K, S>(
    source1: LiveData<T>,
    source2: LiveData<K>,
    private val combine: (data1: T, data2: K) -> S,
) : MediatorLiveData<S>() {
    private var data1: T? = null
    private var data2: K? = null

    init {
        super.addSource(source1) {
            data1 = it
            emitIfNecessary()
        }
        super.addSource(source2) {
            data2 = it
            emitIfNecessary()
        }
    }

    private fun emitIfNecessary() {
        val currentData1 = data1
        val currentData2 = data2
        if (currentData1 != null && currentData2 != null) {
            value = combine(currentData1, currentData2)
        }
    }

    override fun <S : Any?> addSource(
        source: LiveData<S>,
        onChanged: Observer<in S>,
    ) {
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> removeSource(toRemote: LiveData<T>) {
        throw UnsupportedOperationException()
    }
}
