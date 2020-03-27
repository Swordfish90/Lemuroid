package com.swordfish.lemuroid.app.tv.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.app.shared.library.LibraryIndexMonitor

class MainTVViewModel(appContext: Context) : ViewModel() {

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainTVViewModel(appContext) as T
        }
    }

    val indexingInProgress = LibraryIndexMonitor(appContext).getLiveData()
}
