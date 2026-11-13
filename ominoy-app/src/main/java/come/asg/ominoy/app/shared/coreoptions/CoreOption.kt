package come.asg.ominoy.app.shared.coreoptions

import com.swordfish.libretrodroid.Variable
import come.asg.ominoy.lib.core.CoreVariable
import java.io.Serializable

data class CoreOption(
    val variable: CoreVariable,
    val name: String,
    val optionValues: List<String>,
) : Serializable {
    companion object {
        fun fromLibretroDroidVariable(variable: Variable): CoreOption {
            val name = variable.description?.split(";")?.get(0)!!
            val values =
                variable.description
                    ?.split(";")
                    ?.get(1)
                    ?.trim()
                    ?.split('|') ?: listOf()
            val coreVariable = CoreVariable(variable.key!!, variable.value!!)
            return CoreOption(coreVariable, name, values)
        }
    }
}
