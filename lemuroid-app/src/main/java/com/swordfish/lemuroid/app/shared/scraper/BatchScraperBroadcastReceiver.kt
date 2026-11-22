package com.swordfish.lemuroid.app.shared.scraper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.swordfish.lemuroid.R

class BatchScraperBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        context?.applicationContext?.let { appCtx ->
            BatchScraperScheduler.cancelBatchScrape(appCtx)
            Toast.makeText(
                appCtx,
                appCtx.getString(R.string.toast_scrape_cancelled),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}
