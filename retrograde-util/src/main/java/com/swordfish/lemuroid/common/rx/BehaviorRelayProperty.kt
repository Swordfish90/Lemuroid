package com.swordfish.lemuroid.common.rx

import com.jakewharton.rxrelay2.BehaviorRelay
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BehaviorRelayProperty<T>(
    private val subject: BehaviorRelay<T>
) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = subject.value!!
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = subject.accept(value)
}
