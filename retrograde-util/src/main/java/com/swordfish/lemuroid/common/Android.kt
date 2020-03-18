package com.swordfish.lemuroid.common

import android.os.Bundle


fun Bundle?.dump(): String {
    if (this == null) return "null"

    val builder = StringBuilder("Extras:\n")
    for (key in keySet()) {
        val value: Any? = get(key)
        builder.append(key).append(": ").append(value).append("\n") //add the key-value pair to the
    }
    return builder.toString()
}
