package come.asg.ominoy.app.tv.settings

import android.content.Context
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import come.asg.ominoy.R
import come.asg.ominoy.lib.bios.Bios
import come.asg.ominoy.lib.bios.BiosManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BiosPreferences(
    private val biosManager: BiosManager,
) {
    suspend fun refreshBiosPreferences(preferenceScreen: PreferenceScreen) {
        val biosInfo = withContext(Dispatchers.IO) { biosManager.getBiosInfoAsync() }
        val context = preferenceScreen.context
        preferenceScreen.removeAll()
        val installedBios = biosInfo.detected
        val notInstalledBios = biosInfo.notDetected
        val checksumMismatches = biosInfo.checksumMismatches.toSet()

        preferenceScreen.addPreference(
            Preference(context).apply {
                summary = context.getString(R.string.bios_settings_help)
                isSelectable = false
                isIconSpaceReserved = false
            },
        )

        val detectedBios = createCategory(context, context.getString(R.string.settings_bios_category_detected))
        preferenceScreen.addPreference(detectedBios)
        detectedBios.isVisible = installedBios.isNotEmpty()

        val notDetectedBios = createCategory(context, context.getString(R.string.settings_bios_category_not_detected))
        preferenceScreen.addPreference(notDetectedBios)
        notDetectedBios.isVisible = notInstalledBios.isNotEmpty()

        installedBios.forEach {
            val preference = createBiosPreference(preferenceScreen.context, it)
            preference.isEnabled = true
            preference.summary =
                "${context.getString(R.string.bios_status_detected)} · ${it.displayName()}"
            detectedBios.addPreference(preference)
        }

        notInstalledBios.forEach {
            val preference = createBiosPreference(preferenceScreen.context, it)
            preference.isEnabled = false
            preference.summary =
                if (it in checksumMismatches) {
                    "${context.getString(R.string.bios_status_checksum_mismatch)} · ${it.displayName()}"
                } else {
                    "${context.getString(R.string.bios_status_not_found)} · ${it.displayName()}"
                }
            notDetectedBios.addPreference(preference)
        }
    }

    private fun createBiosPreference(
        context: Context,
        bios: Bios,
    ): Preference {
        val preference = Preference(context)
        preference.title = bios.description
        preference.summary = bios.displayName()
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun createCategory(
        context: Context,
        title: String,
    ): PreferenceCategory {
        val category = PreferenceCategory(context)
        category.title = title
        category.isIconSpaceReserved = false
        return category
    }
}
