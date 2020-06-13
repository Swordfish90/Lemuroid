package com.swordfish.lemuroid.common

import android.content.Context
import android.os.Bundle
import android.util.TypedValue

fun Context.getAccentColor(): Int {
    val outValue = TypedValue()
    this.theme.resolveAttribute(android.R.attr.colorAccent, outValue, true)
    return outValue.data
}

fun Bundle?.dump(): String {
    if (this == null) return "null"

    val builder = StringBuilder("Extras:\n")
    keySet()
        .forEach { key ->
            builder.append(key).append(": ").append(get(key)).append("\n")
        }
    return builder.toString()
}
