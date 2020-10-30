package com.swordfish.lemuroid.common.kotlin

data class NTuple4<T1, T2, T3, T4>(val t1: T1, val t2: T2, val t3: T3, val t4: T4)

fun Long.toStringCRC32(): String {
    return "%08x".format(this).toUpperCase()
}
