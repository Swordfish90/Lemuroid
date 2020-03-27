package com.swordfish.lemuroid.common

import android.os.Bundle

fun Bundle?.dump(): String {
    if (this == null) return "null"

    val builder = StringBuilder("Extras:\n")
    keySet()
        .forEach { key ->
            builder.append(key).append(": ").append(get(key)).append("\n")
        }
    return builder.toString()
}
