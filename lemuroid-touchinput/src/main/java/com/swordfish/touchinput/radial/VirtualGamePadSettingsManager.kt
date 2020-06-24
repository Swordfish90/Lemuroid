package com.swordfish.touchinput.radial

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.swordfish.lemuroid.common.kotlin.SharedPreferencesDelegates
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.touchinput.controller.R
import kotlin.math.roundToInt

class VirtualGamePadSettingsManager(private val context: Context, private val systemID: SystemID) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var landscapeOffsetY: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_offset_y_landscape),
        floatToIndex(DEFAULT_LANDSCAPE_OFFSET_Y),
        100
    )

    var landscapeScale: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_scale_landscape),
        floatToIndex(DEFAULT_LANDSCAPE_SCALE),
        100
    )

    var landscapeRotation: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_rotation_landscape),
        floatToIndex(DEFAULT_LANDSCAPE_ROTATION),
        100
    )

    var portraitScale: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_scale_portrait),
        floatToIndex(DEFAULT_PORTRAIT_SCALE),
        100
    )

    var portraitRotation: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_rotation_portrait),
        floatToIndex(DEFAULT_PORTRAIT_ROTATION),
        100
    )

    fun resetLandscape() {
        landscapeScale = DEFAULT_LANDSCAPE_SCALE
        landscapeRotation = DEFAULT_LANDSCAPE_ROTATION
        landscapeOffsetY = DEFAULT_LANDSCAPE_OFFSET_Y
    }

    fun resetPortrait() {
        portraitScale = DEFAULT_PORTRAIT_SCALE
        portraitRotation = DEFAULT_PORTRAIT_ROTATION
    }

    private fun floatToIndex(value: Float): Int = (value * 100).roundToInt()

    companion object {
        const val DEFAULT_LANDSCAPE_SCALE = 0.5f
        const val DEFAULT_LANDSCAPE_ROTATION = 0.0f
        const val DEFAULT_LANDSCAPE_OFFSET_Y = 0.0f

        const val DEFAULT_PORTRAIT_SCALE = 0.5f
        const val DEFAULT_PORTRAIT_ROTATION = 0.0f
    }

    private fun getPreferenceString(preferenceStringId: Int): String {
        return "${context.getString(preferenceStringId)}_$systemID"
    }
}
