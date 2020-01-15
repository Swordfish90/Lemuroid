package com.swordfish.lemuroid.common.kotlin

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun SharedPreferences.boolDelegate(
        key: (KProperty<*>) -> String = KProperty<*>::name,
        defaultValue: Boolean
): ReadWriteProperty<Any, Boolean> {
    return object : ReadWriteProperty<Any, Boolean> {

        override fun getValue(thisRef: Any, property: KProperty<*>) = getBoolean(key(property), defaultValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) =
            edit().putBoolean(key(property), value).apply()
    }
}
