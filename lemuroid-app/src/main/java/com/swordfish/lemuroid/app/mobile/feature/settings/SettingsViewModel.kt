package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.library.PendingOperationsMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class SettingsViewModel(
    context: Context,
    directoryPreference: String,
    sharedPreferences: FlowSharedPreferences
) : ViewModel() {

    class Factory(
        private val context: Context,
        private val sharedPreferences: FlowSharedPreferences
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val directoryPreference = context.getString(R.string.pref_key_extenral_folder)
            return SettingsViewModel(context, directoryPreference, sharedPreferences) as T
        }
    }

    val currentFolder = MutableStateFlow("")

    val indexingInProgress = PendingOperationsMonitor(context).anyLibraryOperationInProgress()

    val directoryScanInProgress = PendingOperationsMonitor(context).isDirectoryScanInProgress()

    init {
        viewModelScope.launch {
            sharedPreferences.getString(directoryPreference).asFlow()
                .flowOn(Dispatchers.IO)
                .collect { currentFolder.value = it }
        }
    }
}
