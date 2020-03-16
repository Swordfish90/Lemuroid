package com.swordfish.lemuroid.app.tv

import android.content.Context
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.app.feature.library.LibraryIndexMonitor
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase

class MainTVViewModel(appContext: Context) : ViewModel() {

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainTVViewModel(appContext) as T
        }
    }

    val indexingInProgress = LibraryIndexMonitor(appContext).getLiveData()
}
