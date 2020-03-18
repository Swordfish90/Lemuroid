package com.swordfish.lemuroid.app.feature.coreoptions

import android.content.Context
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.swordfish.lemuroid.lib.core.CoreVariablesManager

object CoreOptionsPreferenceHelper {

    private val BOOLEAN_SET = setOf("enabled", "disabled")

    fun convertToPreference(context: Context, it: CoreOption, systemID: String): Preference {
        return if (it.optionValues.toSet() == BOOLEAN_SET) {
            buildSwitchPreference(context, it, systemID)
        } else {
            buildListPreference(context, it, systemID)
        }
    }

    private fun buildListPreference(context: Context, it: CoreOption, systemID: String): ListPreference {
        val preference = ListPreference(context)
        preference.key = CoreVariablesManager.computeSharedPreferenceKey(it.variable.key, systemID)
        preference.title = it.name
        preference.entries = it.optionValues.map { it.capitalize() }.toTypedArray()
        preference.entryValues = it.optionValues.toTypedArray()
        preference.setValueIndex(it.optionValues.indexOf(it.variable.value))
        preference.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun buildSwitchPreference(context: Context, it: CoreOption, systemID: String): SwitchPreference {
        val preference = SwitchPreference(context)
        preference.key = CoreVariablesManager.computeSharedPreferenceKey(it.variable.key, systemID)
        preference.title = it.name
        preference.isChecked = it.variable.value == "enabled"
        preference.isIconSpaceReserved = false
        return preference
    }
}
