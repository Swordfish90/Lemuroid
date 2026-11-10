package com.swordfish.lemuroid.app.shared.diagnostics

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.swordfish.lemuroid.R

class DiagnosticLogActivity : Activity() {
    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }
        root.addView(TextView(this).apply {
            text = getString(R.string.diagnostics_title)
            textSize = 22f
        })
        root.addView(TextView(this).apply {
            text = getString(R.string.diagnostics_description)
            textSize = 14f
            setPadding(0, 8, 0, 16)
        })

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(Button(this).apply {
            text = getString(R.string.diagnostics_refresh)
            setOnClickListener { refreshLog() }
        })
        actions.addView(Button(this).apply {
            text = getString(R.string.diagnostics_share)
            setOnClickListener { shareLog() }
        })
        actions.addView(Button(this).apply {
            text = getString(R.string.diagnostics_clear)
            setOnClickListener {
                if (AppDiagnosticLog.clear(this@DiagnosticLogActivity)) {
                    refreshLog()
                    Toast.makeText(this@DiagnosticLogActivity, R.string.diagnostics_cleared, Toast.LENGTH_SHORT).show()
                }
            }
        })
        root.addView(actions, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        logView = TextView(this).apply {
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
            setTextIsSelectable(true)
            setPadding(0, 16, 0, 16)
        }
        root.addView(ScrollView(this).apply {
            addView(logView)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)
        refreshLog()
    }

    private fun refreshLog() {
        logView.text = AppDiagnosticLog.read(this)
    }

    private fun shareLog() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Lemuroid - diagnóstico")
            putExtra(Intent.EXTRA_TEXT, AppDiagnosticLog.read(this@DiagnosticLogActivity))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.diagnostics_share)))
    }
}
