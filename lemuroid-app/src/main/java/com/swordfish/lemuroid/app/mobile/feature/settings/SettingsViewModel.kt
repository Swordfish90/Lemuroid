package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.library.LibraryIndexMonitor

class SettingsViewModel(
    context: Context,
    directoryPreference: String,
    rxSharedPreferences: RxSharedPreferences
) : ViewModel() {

    class Factory(
        private val context: Context,
        private val rxSharedPreferences: RxSharedPreferences
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val directoryPreference = context.getString(R.string.pref_key_extenral_folder)
            return SettingsViewModel(context, directoryPreference, rxSharedPreferences) as T
        }
    }

    val currentFolder = rxSharedPreferences.getString(directoryPreference)
            .asObservable()
            .filter { it.isNotBlank() }

    val indexingInProgress = LibraryIndexMonitor(context).getLiveData()
}
