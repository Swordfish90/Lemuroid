package com.swordfish.lemuroid.common.livedata

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
