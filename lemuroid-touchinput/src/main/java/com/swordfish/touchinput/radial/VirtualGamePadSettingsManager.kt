package com.swordfish.touchinput.radial

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.common.kotlin.SharedPreferencesDelegates
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.touchinput.controller.R

class VirtualGamePadSettingsManager(private val context: Context, private val systemID: SystemID) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var offsetX: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_offset_x),
        10,
        100
    )

    var offsetY: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_offset_y),
        10,
        100
    )

    var scale: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_scale),
        50,
        100
    )

    var rotation: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_rotation),
        0,
        100
    )

    private fun getPreferenceString(preferenceStringId: Int): String {
        return "${context.getString(preferenceStringId)}_$systemID"
    }
}
