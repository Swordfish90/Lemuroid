package com.swordfish.lemuroid.app.shared.coreoptions

import android.content.Context
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.settings.ControllerConfigsManager
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import com.swordfish.lemuroid.lib.library.CoreID

object CoreOptionsPreferenceHelper {

    private val BOOLEAN_SET = setOf("enabled", "disabled")

    fun addPreferences(
        preferenceScreen: PreferenceScreen,
        systemID: String,
        baseOptions: List<CoreOption>,
        advancedOptions: List<CoreOption>
    ) {
        if (baseOptions.isEmpty() && advancedOptions.isEmpty()) {
            return
        }

        val context = preferenceScreen.context

        val title = context.getString(R.string.core_settings_category_preferences)
        val preferencesCategory = createCategory(preferenceScreen.context, preferenceScreen, title)

        addPreferences(context, preferencesCategory, baseOptions, systemID)
        addPreferences(context, preferencesCategory, advancedOptions, systemID)
    }

    fun addControllers(
        preferenceScreen: PreferenceScreen,
        systemID: String,
        coreID: CoreID,
        controllers: Map<Int, List<ControllerConfig>>
    ) {
        val visibleControllers = controllers.entries
            .filter { (_, controllers) -> controllers.size >= 2 }

        if (visibleControllers.isEmpty()) {
            return
        }

        val context = preferenceScreen.context
        val title = context.getString(R.string.core_settings_category_controllers)
        val category = createCategory(context, preferenceScreen, title)

        visibleControllers
            .forEach { (port, controllers) ->
                val preference = buildControllerPreference(context, systemID, coreID, port, controllers)
                category.addPreference(preference)
            }
    }

    private fun addPreferences(
        context: Context,
        preferenceGroup: PreferenceGroup,
        options: List<CoreOption>,
        systemID: String
    ) {
        options
            .map { convertToPreference(context, it, systemID) }
            .forEach { preferenceGroup.addPreference(it) }
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

    private fun buildControllerPreference(
        context: Context,
        systemID: String,
        coreID: CoreID,
        port: Int,
        controllerConfigs: List<ControllerConfig>
    ): Preference {
        val preference = ListPreference(context)
        preference.key = ControllerConfigsManager.getSharedPreferencesId(systemID, coreID, port)
        preference.title = context.getString(R.string.core_settings_controller, (port + 1).toString())
        preference.entries = controllerConfigs.map { context.getString(it.displayName) }.toTypedArray()
        preference.entryValues = controllerConfigs.map { it.name }.toTypedArray()
        preference.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        preference.isIconSpaceReserved = false
        preference.setDefaultValue(controllerConfigs.map { it.name }.first())
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
