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
    val BOOLEAN_SET = setOf("enabled", "disabled")

    fun addPreferences(
        preferenceScreen: PreferenceScreen,
        systemID: String,
        baseOptions: List<LemuroidCoreOption>,
        advancedOptions: List<LemuroidCoreOption>,
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
        connectedGamePads: Int,
        controllers: Map<Int, List<ControllerConfig>>,
    ) {
        val visibleControllers =
            (0 until connectedGamePads)
                .map { it to controllers[it] }
                .filter { (_, controllers) -> controllers != null && controllers.size >= 2 }

        if (visibleControllers.isEmpty()) {
            return
        }

        val context = preferenceScreen.context
        val title = context.getString(R.string.core_settings_category_controllers)
        val category = createCategory(context, preferenceScreen, title)

        visibleControllers
            .forEach { (port, controllers) ->
                val preference = buildControllerPreference(context, systemID, coreID, port, controllers!!)
                category.addPreference(preference)
            }
    }

    private fun addPreferences(
        context: Context,
        preferenceGroup: PreferenceGroup,
        options: List<LemuroidCoreOption>,
        systemID: String,
    ) {
        options
            .map { convertToPreference(context, it, systemID) }
            .forEach { preferenceGroup.addPreference(it) }
    }

    private fun convertToPreference(
        context: Context,
        it: LemuroidCoreOption,
        systemID: String,
    ): Preference {
        return if (it.getEntriesValues().toSet() == BOOLEAN_SET) {
            buildSwitchPreference(context, it, systemID)
        } else {
            buildListPreference(context, it, systemID)
        }
    }

    private fun buildListPreference(
        context: Context,
        it: LemuroidCoreOption,
        systemID: String,
    ): ListPreference {
        val preference = ListPreference(context)
        preference.key = CoreVariablesManager.computeSharedPreferenceKey(it.getKey(), systemID)
        preference.title = it.getDisplayName(context)
        preference.entries = it.getEntries(context).toTypedArray()
        preference.entryValues = it.getEntriesValues().toTypedArray()
        preference.setDefaultValue(it.getCurrentValue())
        preference.setValueIndex(it.getCurrentIndex())
        preference.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun buildSwitchPreference(
        context: Context,
        it: LemuroidCoreOption,
        systemID: String,
    ): SwitchPreference {
        val preference = SwitchPreference(context)
        preference.key = CoreVariablesManager.computeSharedPreferenceKey(it.getKey(), systemID)
        preference.title = it.getDisplayName(context)
        preference.setDefaultValue(it.getCurrentValue() == "enabled")
        preference.isChecked = it.getCurrentValue() == "enabled"
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun buildControllerPreference(
        context: Context,
        systemID: String,
        coreID: CoreID,
        port: Int,
        controllerConfigs: List<ControllerConfig>,
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
        title: String,
    ): PreferenceCategory {
        val category = PreferenceCategory(context)
        preferenceScreen.addPreference(category)
        category.title = title
        category.isIconSpaceReserved = false
        return category
    }
}
