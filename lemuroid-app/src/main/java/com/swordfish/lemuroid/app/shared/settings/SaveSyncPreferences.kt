package com.swordfish.lemuroid.app.shared.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager

class SaveSyncPreferences(private val saveSyncManager: SaveSyncManager) {
    fun addSaveSyncPreferences(preferenceScreen: PreferenceScreen) {
        val context = preferenceScreen.context

        Preference(context).apply {
            key = keyConfigure(context)
            preferenceScreen.addPreference(this)
        }

        SwitchPreference(context).apply {
            key = keySyncEnabled(context)
            preferenceScreen.addPreference(this)
        }

        MultiSelectListPreference(context).apply {
            key = keySyncCores(context)
            preferenceScreen.addPreference(this)
        }

        SwitchPreference(context).apply {
            key = keyAutoSync(context)
            preferenceScreen.addPreference(this)
        }

        Preference(context).apply {
            key = keyForceSync(context)
            preferenceScreen.addPreference(this)
        }

        updatePreferences(preferenceScreen, false)
    }

    fun updatePreferences(
        preferenceScreen: PreferenceScreen,
        syncInProgress: Boolean,
    ) {
        val context = preferenceScreen.context

        preferenceScreen.findPreference<Preference>(keyConfigure(context))?.apply {
            title =
                context.getString(
                    R.string.settings_save_sync_configure,
                    saveSyncManager.getProvider(),
                )
            isIconSpaceReserved = false
            isEnabled = !syncInProgress
            summary = saveSyncManager.getConfigInfo()
        }

        preferenceScreen.findPreference<Preference>(keySyncEnabled(context))?.apply {
            title = context.getString(R.string.settings_save_sync_include_saves)
            summary =
                context.getString(
                    R.string.settings_save_sync_include_saves_description,
                    saveSyncManager.computeSavesSpace(),
                )
            isEnabled = saveSyncManager.isConfigured() && !syncInProgress
            isIconSpaceReserved = false
        }

        preferenceScreen.findPreference<Preference>(keyAutoSync(context))?.apply {
            title = context.getString(R.string.settings_save_sync_enable_auto)
            isEnabled = saveSyncManager.isConfigured() && !syncInProgress
            summary = context.getString(R.string.settings_save_sync_enable_auto_description)
            dependency = keySyncEnabled(context)
            isIconSpaceReserved = false
        }

        preferenceScreen.findPreference<Preference>(keyForceSync(context))?.apply {
            title = context.getString(R.string.settings_save_sync_refresh)
            isEnabled = saveSyncManager.isConfigured() && !syncInProgress
            summary =
                context.getString(
                    R.string.settings_save_sync_refresh_description,
                    saveSyncManager.getLastSyncInfo(),
                )
            dependency = keySyncEnabled(context)
            isIconSpaceReserved = false
        }

        preferenceScreen.findPreference<MultiSelectListPreference>(keySyncCores(context))?.apply {
            title = context.getString(R.string.settings_save_sync_include_states)
            summary = context.getString(R.string.settings_save_sync_include_states_description)
            dependency = keySyncEnabled(context)
            isEnabled = saveSyncManager.isConfigured() && !syncInProgress
            entries =
                CoreID.values()
                    .map { saveSyncManager.getDisplayNameForCore(context, it) }
                    .toTypedArray()
            entryValues = CoreID.values().map { it.coreName }.toTypedArray()
            isIconSpaceReserved = false
        }
    }

    fun onPreferenceTreeClick(
        activity: Activity?,
        preference: Preference,
    ): Boolean {
        val context = preference.context
        return when (preference.key) {
            keyConfigure(context) -> {
                handleSaveSyncConfigure(activity)
                true
            }
            keyForceSync(context) -> {
                handleSaveSyncRefresh(context)
                true
            }
            else -> false
        }
    }

    private fun keySyncEnabled(context: Context) = context.getString(R.string.pref_key_save_sync_enable)

    private fun keyForceSync(context: Context) = context.getString(R.string.pref_key_save_sync_force_refresh)

    private fun keyConfigure(context: Context) = context.getString(R.string.pref_key_save_sync_configure)

    private fun keyAutoSync(context: Context) = context.getString(R.string.pref_key_save_sync_auto)

    private fun keySyncCores(context: Context) = context.getString(R.string.pref_key_save_sync_cores)

    private fun handleSaveSyncConfigure(activity: Activity?) {
        activity?.startActivity(
            Intent(activity, saveSyncManager.getSettingsActivity()),
        )
    }

    private fun handleSaveSyncRefresh(context: Context) {
        SaveSyncWork.enqueueManualWork(context.applicationContext)
    }
}
