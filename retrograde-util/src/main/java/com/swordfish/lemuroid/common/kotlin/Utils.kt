package com.swordfish.lemuroid.common.kotlin

fun Long.toStringCRC32(): String {
    return "%x".format(this).toUpperCase()
}
