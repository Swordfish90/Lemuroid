package com.swordfish.lemuroid.app.shared.library

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CoreUpdateBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        LibraryIndexScheduler.cancelCoreUpdate(context!!.applicationContext)
    }
}
