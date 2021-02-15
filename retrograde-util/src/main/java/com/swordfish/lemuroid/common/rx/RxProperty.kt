package com.swordfish.lemuroid.common.rx

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

class RxProperty<T : Any>(defaultValue: T) : ReadWriteProperty<Any, T> {

    val relay = BehaviorRelay.createDefault(defaultValue)

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return relay.value!!
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        relay.accept(value)
    }
}

fun <T : Any> KProperty0<T>.asObservable(): Observable<T> {
    isAccessible = true

    return getDelegate()?.let {
        if (it is RxProperty<*>) {
            return it.relay as Observable<T>
        }
        throw IllegalAccessException()
    } ?: Observable.empty()
}
