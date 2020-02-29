package com.swordfish.lemuroid.app.feature.coreoptions

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.core.CoreVariablesManager
import java.security.InvalidParameterException

class CoreOptionsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.core_preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val coreOptions = arguments?.getSerializable(EXTRA_CORE_OPTIONS) as Array<CoreOption>?
                ?: throw InvalidParameterException("Missing EXTRA_CORE_OPTIONS")

        val systemID = arguments?.getString(EXTRA_SYSTEM_ID)
                ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_ID")

        coreOptions
            .map { convertToPreference(it, systemID) }
            .forEach { preferenceScreen.addPreference(it) }
    }

    private fun convertToPreference(it: CoreOption, systemID: String): Preference {
        return if (it.optionValues.toSet() == BOOLEAN_SET) {
            buildSwitchPreference(it, systemID)
        } else {
            buildListPreference(it, systemID)
        }
    }

    private fun buildListPreference(it: CoreOption, systemID: String): ListPreference {
        val preference = ListPreference(preferenceScreen.context)
        preference.key = CoreVariablesManager.computeSharedPreferenceKey(it.variable.key, systemID)
        preference.title = it.name
        preference.entries = it.optionValues.map { it.capitalize() }.toTypedArray()
        preference.entryValues = it.optionValues.toTypedArray()
        preference.setValueIndex(it.optionValues.indexOf(it.variable.value))
        preference.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun buildSwitchPreference(it: CoreOption, systemID: String): SwitchPreference {
        val preference = SwitchPreference(preferenceScreen.context)
        preference.key = CoreVariablesManager.computeSharedPreferenceKey(it.variable.key, systemID)
        preference.title = it.name
        preference.isChecked = it.variable.value == "enabled"
        preference.isIconSpaceReserved = false
        return preference
    }

    companion object {
        private const val EXTRA_CORE_OPTIONS = "core_options"
        private const val EXTRA_SYSTEM_ID = "system_id"
        private val BOOLEAN_SET = setOf("enabled", "disabled")

        fun newInstance(options: Array<CoreOption>, systemID: String): CoreOptionsFragment {
            val result = CoreOptionsFragment()
            result.arguments = Bundle().apply {
                putSerializable(EXTRA_CORE_OPTIONS, options)
                putString(EXTRA_SYSTEM_ID, systemID)
            }
            return result
        }
    }
}
