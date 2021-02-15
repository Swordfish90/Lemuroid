package com.swordfish.lemuroid.common.rx

import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

class RxNullableProperty<T : Any>(defaultValue: T?) : ReadWriteProperty<Any, T?> {

    val relay = BehaviorRelay.createDefault(defaultValue.toOptional())

    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        return relay.value?.toNullable()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        relay.accept(value.toOptional())
    }
}

fun <T : Any> KProperty0<T?>.asNullableObservable(): Observable<Optional<T>> {
    isAccessible = true

    return getDelegate()?.let {
        if (it is RxNullableProperty<*>) {
            return it.relay as Observable<Optional<T>>
        }
        throw IllegalAccessException()
    } ?: Observable.empty()
}
