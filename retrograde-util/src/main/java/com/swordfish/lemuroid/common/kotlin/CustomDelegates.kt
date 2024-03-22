package com.swordfish.lemuroid.common.kotlin

import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

object CustomDelegates {
    inline fun <T> onChangeObservable(
        initialValue: T,
        crossinline onChange: () -> Unit,
    ): ReadWriteProperty<Any?, T> = Delegates.observable(initialValue) { _, old, new -> if (old != new) onChange() }
}
