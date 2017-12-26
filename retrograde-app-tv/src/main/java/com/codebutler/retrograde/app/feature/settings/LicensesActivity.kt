package com.codebutler.retrograde.app.feature.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import com.codebutler.retrograde.R
import com.codebutler.retrograde.common.kotlin.bindView
import com.codebutler.retrograde.lib.android.RetrogradeActivity

class LicensesActivity : RetrogradeActivity() {

    private val webView by bindView<WebView>(R.id.webView)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)
        webView.loadUrl("file:///android_res/raw/licenses.html")
        webView.settings.javaScriptEnabled = true
    }
}
