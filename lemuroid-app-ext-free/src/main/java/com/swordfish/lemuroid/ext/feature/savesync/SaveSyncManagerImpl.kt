package com.swordfish.lemuroid.ext.feature.savesync

import android.app.Activity
import android.content.Context
import com.swordfish.lemuroid.common.kotlin.SharedPreferencesDelegates
import com.swordfish.lemuroid.ext.R
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat

class SaveSyncManagerImpl(
    private val appContext: Context,
    private val directoriesManager: DirectoriesManager
) : SaveSyncManager {

    private var lastSyncTimestamp: Long by SharedPreferencesDelegates.LongDelegate(
        SharedPreferencesHelper.getSharedPreferences(appContext),
        appContext.getString(R.string.pref_key_last_save_sync),
        0L
    )

    private var storageUri: String by SharedPreferencesDelegates.StringDelegate(
        SharedPreferencesHelper.getSharedPreferences(appContext),
        appContext.getString(R.string.pref_key_saf_uri),
        ActivateSAFActivity.PREF_KEY_STORAGE_URI_NONE
    )


    // done
    override fun getProvider(): String = "SAF"

    // done
    override fun getSettingsActivity(): Class<out Activity>? = ActivateSAFActivity::class.java

    // done
    override fun isSupported(): Boolean = true


    // todo, check if pref has been set
    override fun isConfigured(): Boolean {
        return storageUri != ActivateSAFActivity.PREF_KEY_STORAGE_URI_NONE
    }

    // done
    override fun getLastSyncInfo(): String {
        val dateString = if (lastSyncTimestamp > 0) {
            SimpleDateFormat.getDateTimeInstance().format(lastSyncTimestamp)
        } else {
            "-"
        }
        return appContext.getString(R.string.saf_last_sync_completed, dateString)
    }

    // not needed
    override fun getConfigInfo(): String = ""

    override suspend fun sync(cores: Set<CoreID>) {
        synchronized(SYNC_LOCK) {
            val saveSyncResult = runCatching {
                performSaveSyncForCores(cores)
            }

            saveSyncResult.onFailure {
                Timber.e(it, "Error while performing save sync.")
            }
        }
    }


    private fun performSaveSyncForCores(cores: Set<CoreID>) {

        lastSyncTimestamp = System.currentTimeMillis()
    }

    override fun computeSavesSpace() = ""

    override fun computeStatesSpace(coreID: CoreID) = ""


    companion object {
        const val GDRIVE_PROPERTY_LOCAL_PATH = "localPath"
        private val SYNC_LOCK = Object()
    }
}
