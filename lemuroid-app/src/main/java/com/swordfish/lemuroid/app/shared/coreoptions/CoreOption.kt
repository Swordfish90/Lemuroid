package com.swordfish.lemuroid.app.shared.coreoptions

import com.swordfish.lemuroid.lib.core.CoreVariable
import com.swordfish.libretrodroid.Variable
import java.io.Serializable

data class CoreOption(
    val variable: CoreVariable,
    val name: String,
    val optionValues: List<String>,
) : Serializable {
    companion object {
        fun fromLibretroDroidVariable(variable: Variable): CoreOption {
            val key = variable.key ?: ""
            val value = variable.value ?: ""
            val desc = variable.description ?: ""
            val parts = desc.split(";")
            val name = parts.getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() } ?: key
            val values = parts.getOrNull(1)?.trim()?.split('|')?.filter { it.isNotEmpty() } ?: listOf()
            val coreVariable = CoreVariable(key, value)
            return CoreOption(coreVariable, name, values)
        }
    }
}
