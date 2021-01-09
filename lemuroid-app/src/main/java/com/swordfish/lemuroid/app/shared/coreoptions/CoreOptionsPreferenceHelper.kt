package com.swordfish.lemuroid.app.shared.coreoptions

import android.content.Context
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.core.CoreVariablesManager

object CoreOptionsPreferenceHelper {

    private val BOOLEAN_SET = setOf("enabled", "disabled")

    fun addPreferences(
        preferenceScreen: PreferenceScreen,
        systemID: String,
        baseOptions: List<CoreOption>,
        advancedOptions: List<CoreOption>
    ) {
        val context = preferenceScreen.context
        if (baseOptions.isNotEmpty()) {
            val title = context.getString(R.string.core_settings_category_basic)
            initializeCategory(preferenceScreen, title, baseOptions, systemID)
        }

        if (advancedOptions.isNotEmpty()) {
            val title = context.getString(R.string.core_settings_category_advanced)
            initializeCategory(preferenceScreen, title, advancedOptions, systemID)
        }
    }

    private fun initializeCategory(
        preferenceScreen: PreferenceScreen,
        title: String,
        baseOptions: List<CoreOption>,
        systemID: String
    ) {
        val baseCategory = createCategory(preferenceScreen.context, preferenceScreen, title)
        baseOptions
            .map { convertToPreference(preferenceScreen.context, it, systemID) }
            .forEach { baseCategory.addPreference(it) }
    }

    private fun convertToPreference(context: Context, it: CoreOption, systemID: String): Preference {
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
        preference.setDefaultValue(it.variable.value)
        preference.setValueIndex(it.optionValues.indexOf(it.variable.value))
        preference.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun buildSwitchPreference(context: Context, it: CoreOption, systemID: String): SwitchPreference {
        val preference = SwitchPreference(context)
        preference.key = CoreVariablesManager.computeSharedPreferenceKey(it.variable.key, systemID)
        preference.title = it.name
        preference.setDefaultValue(it.variable.value == "enabled")
        preference.isChecked = it.variable.value == "enabled"
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun createCategory(
        context: Context,
        preferenceScreen: PreferenceScreen,
        title: String
    ): PreferenceCategory {
        val category = PreferenceCategory(context)
        preferenceScreen.addPreference(category)
        category.title = title
        category.isIconSpaceReserved = false
        return category
    }
}
