package com.swordfish.lemuroid.lib.cheats

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
data class CheatEntry(
    val code: String,
    val description: String,
    val enabled: Boolean,
) : JavaSerializable
