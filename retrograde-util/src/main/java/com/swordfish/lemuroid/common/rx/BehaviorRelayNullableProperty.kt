package com.swordfish.lemuroid.common.rx

import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import com.jakewharton.rxrelay2.BehaviorRelay
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BehaviorRelayNullableProperty<T : Any>(
    private val subject: BehaviorRelay<Optional<T>>
) : ReadWriteProperty<Any?, T?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = subject.value?.toNullable()
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) = subject.accept(value.toOptional())
}
