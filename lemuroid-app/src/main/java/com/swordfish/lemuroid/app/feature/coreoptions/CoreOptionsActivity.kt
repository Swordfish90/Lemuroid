package com.swordfish.lemuroid.app.feature.coreoptions

import android.os.Bundle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.android.RetrogradeAppCompatActivity
import java.security.InvalidParameterException

class CoreOptionsActivity : RetrogradeAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_empty)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (savedInstanceState == null) {
            val options = intent.extras?.getSerializable(EXTRA_RETRO_OPTIONS) as Array<CoreOption>?
                    ?: throw InvalidParameterException("Missing EXTRA_RETRO_OPTIONS")

            val systemID = intent.extras?.getString(EXTRA_SYSTEM_ID)
                    ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_ID")

            supportFragmentManager.beginTransaction()
                    .add(R.id.content, CoreOptionsFragment.newInstance(options, systemID)).commit()
        }
    }

    companion object {
        const val EXTRA_RETRO_OPTIONS = "retro_options"
        const val EXTRA_SYSTEM_ID = "system_id"
    }
}
