package com.swordfish.lemuroid.app.tv.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.swordfish.lemuroid.app.tv.shared.TVBaseSettingsActivity

class TVSettingsActivity : TVBaseSettingsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val fragment = TVSettingsFragmentWrapper()
            supportFragmentManager.beginTransaction().replace(android.R.id.content, fragment).commit()
        }
    }

    class TVSettingsFragmentWrapper : BaseSettingsFragmentWrapper() {
        override fun createFragment(): Fragment {
            return TVSettingsFragment()
        }
    }
}
