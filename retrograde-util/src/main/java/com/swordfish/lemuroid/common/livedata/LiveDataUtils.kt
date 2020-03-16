package com.swordfish.lemuroid.common.livedata

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun zipLiveDataWithNull(vararg liveItems: LiveData<*>): LiveData<ArrayList<Any?>> {
    return MediatorLiveData<ArrayList<Any?>>().apply {
        val zippedObjects = arrayOfNulls<Any>(liveItems.size)
        val zippedObjectsFlag = BooleanArray(liveItems.size)
        liveItems.forEachIndexed { index, liveData ->
            addSource(liveData) { item ->
                zippedObjects[index] = item
                zippedObjectsFlag[index] = true
                if (!zippedObjectsFlag.contains(false)) {
                    value = zippedObjects.toCollection(ArrayList())
                    for(i in 0 until liveItems.size) {
                        zippedObjectsFlag[i] = false
                    }
                }
            }
        }
    }
}

fun <T> LiveData<T>.debounce(duration: Long = 1000L) = MediatorLiveData<T>().also { mld ->
    val source = this
    val handler = Handler(Looper.getMainLooper())

    val runnable = Runnable {
        mld.value = source.value
    }

    mld.addSource(source) {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, duration)
    }
}
