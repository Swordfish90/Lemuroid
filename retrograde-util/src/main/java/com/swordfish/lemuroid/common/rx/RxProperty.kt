package com.swordfish.lemuroid.common.rx

import com.jakewharton.rxrelay2.BehaviorRelay
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class RxProperty<T : Any>(defaultValue: T) : ReadWriteProperty<Any, T> {

    private val lock = Any()

    val relay = BehaviorRelay.createDefault(defaultValue)

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return synchronized(lock) {
            relay.value!!
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        synchronized(lock) {
            relay.accept(value)
        }
    }
}
