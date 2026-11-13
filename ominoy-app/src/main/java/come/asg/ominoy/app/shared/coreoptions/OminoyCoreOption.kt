package come.asg.ominoy.app.shared.coreoptions

import android.content.Context
import come.asg.ominoy.lib.library.ExposedSetting
import java.io.Serializable

data class OminoyCoreOption(
    private val exposedSetting: ExposedSetting,
    private val coreOption: CoreOption,
) : Serializable {
    fun getKey(): String = exposedSetting.key

    fun getDisplayName(context: Context): String = context.getString(exposedSetting.titleId)

    fun getEntries(context: Context): List<String> {
        if (exposedSetting.values.isEmpty()) {
            return coreOption.optionValues.map { it.capitalize() }
        }

        return getCorrectExposedSettings().map { context.getString(it.titleId) }
    }

    fun getEntriesValues(): List<String> {
        if (exposedSetting.values.isEmpty()) {
            return coreOption.optionValues.map { it }
        }

        return getCorrectExposedSettings().map { it.key }
    }

    fun getCurrentValue(): String = coreOption.variable.value

    fun getCurrentIndex(): Int = maxOf(getEntriesValues().indexOf(getCurrentValue()), 0)

    private fun getCorrectExposedSettings(): List<ExposedSetting.Value> =
        exposedSetting.values
            .filter { it.key in coreOption.optionValues }
}
