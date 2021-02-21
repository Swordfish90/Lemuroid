package com.swordfish.lemuroid.common.rx

import com.gojuno.koptional.toOptional
import com.jakewharton.rxrelay2.BehaviorRelay
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class RxNullableProperty<T : Any>(defaultValue: T?) : ReadWriteProperty<Any, T?> {

    private val lock = Any()

    val relay = BehaviorRelay.createDefault(defaultValue.toOptional())

    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        return synchronized(lock) {
            relay.value?.toNullable()
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        synchronized(lock) {
            relay.accept(value.toOptional())
        }
    }
}
