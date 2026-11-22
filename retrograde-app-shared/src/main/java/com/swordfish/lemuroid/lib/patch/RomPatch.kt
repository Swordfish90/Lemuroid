package com.swordfish.lemuroid.lib.patch

import java.io.Serializable

enum class PatchSource {
    IN_ZIP,
    EXTERNAL_FILE,
}

data class RomPatch(
    val id: String,
    val name: String,
    val source: PatchSource,
    val location: String,
    val sizeBytes: Long,
    val isValid: Boolean = true,
    val validationError: String? = null,
) : Serializable
