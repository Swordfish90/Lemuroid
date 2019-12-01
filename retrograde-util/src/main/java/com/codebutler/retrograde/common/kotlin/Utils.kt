package com.codebutler.retrograde.common.kotlin

fun Long.toStringCRC32(): String {
    return "%x".format(this).toUpperCase()
}
