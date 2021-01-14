package com.swordfish.lemuroid.lib.library

import com.swordfish.lemuroid.lib.core.CoreVariable
import java.io.Serializable

data class SystemCoreConfig(
    val coreID: CoreID,
    val exposedSettings: List<String> = listOf(),
    val exposedAdvancedSettings: List<String> = listOf(),
    val defaultSettings: List<CoreVariable> = listOf(),
    val statesSupported: Boolean = true
) : Serializable
