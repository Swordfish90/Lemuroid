package com.swordfish.touchinput.radial

import android.content.Context
import android.content.SharedPreferences
import com.swordfish.lemuroid.common.kotlin.SharedPreferencesDelegates
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.touchinput.controller.R
import kotlin.math.roundToInt

class VirtualGamePadSettingsManager(
    private val context: Context,
    private val systemID: SystemID,
    orientation: Orientation
) {
    enum class Orientation {
        PORTRAIT,
        LANDSCAPE
    }

    private val sharedPreferences: SharedPreferences =
        SharedPreferencesHelper.getSharedPreferences(context)

    var scale: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_scale, orientation),
        floatToIndex(DEFAULT_SCALE),
        100
    )

    var rotation: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_rotation, orientation),
        floatToIndex(DEFAULT_ROTATION),
        100
    )

    var marginX: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_margin_x, orientation),
        floatToIndex(DEFAULT_MARGIN_X),
        100
    )

    var marginY: Float by SharedPreferencesDelegates.PercentageDelegate(
        sharedPreferences,
        getPreferenceString(R.string.pref_key_virtual_pad_margin_y, orientation),
        floatToIndex(DEFAULT_MARGIN_Y),
        100
    )

    private fun floatToIndex(value: Float): Int = (value * 100).roundToInt()

    companion object {
        const val DEFAULT_SCALE = 0.5f
        const val DEFAULT_ROTATION = 0.0f
        const val DEFAULT_MARGIN_X = 0.0f
        const val DEFAULT_MARGIN_Y = 0.0f

        const val MAX_ROTATION = 45f
        const val MIN_SCALE = 0.75f
        const val MAX_SCALE = 1.5f

        const val MAX_MARGINS = 96f
    }

    private fun getPreferenceString(preferenceStringId: Int, orientation: Orientation): String {
        return "${context.getString(preferenceStringId)}_${systemID}_${orientation.ordinal}"
    }
}
