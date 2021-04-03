package com.swordfish.lemuroid.lib.library

import java.io.Serializable

data class ExposedSetting(
    val key: String,
    val titleId: Int,
    val values: ArrayList<Value> = arrayListOf(),
) : Serializable {
    data class Value(val key: String, val titleId: Int) : Serializable
}
