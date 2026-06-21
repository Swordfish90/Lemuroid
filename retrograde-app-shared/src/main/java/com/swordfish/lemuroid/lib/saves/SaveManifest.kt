package com.swordfish.lemuroid.lib.saves

import kotlinx.serialization.Serializable

@Serializable
data class SaveManifest(
    val fileName: String,
    val systemId: String,
    val coreName: String,
    val version: Int = 1,
)
