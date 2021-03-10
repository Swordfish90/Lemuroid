package com.swordfish.lemuroid.lib.library

import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.core.CoreVariable
import java.io.Serializable

data class SystemCoreConfig(
    val coreID: CoreID,
    val controllerConfigs: HashMap<Int, ArrayList<ControllerConfig>>,
    val exposedSettings: List<String> = listOf(),
    val exposedAdvancedSettings: List<String> = listOf(),
    val defaultSettings: List<CoreVariable> = listOf(),
    val statesSupported: Boolean = true,
    val requiredBIOSFiles: List<String> = listOf()
) : Serializable
