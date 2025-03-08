package com.swordfish.touchinput.radial.settings

import android.content.SharedPreferences
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.max
import androidx.core.content.edit
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.common.compose.pxToDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class TouchControllerSettingsManager(private val sharedPreferences: SharedPreferences) {
    enum class Orientation {
        PORTRAIT,
        LANDSCAPE,
    }

    @Serializable
    data class Settings(
        val scale: Float = DEFAULT_SCALE,
        val rotation: Float = DEFAULT_ROTATION,
        val marginX: Float = DEFAULT_MARGIN_X,
        val marginY: Float = DEFAULT_MARGIN_Y,
    )

    private fun computeInsetsPaddings(density: Density, insets: WindowInsets): PaddingValues {
        val result = PaddingValues(
            insets.getLeft(density, layoutDirection = LayoutDirection.Ltr).pxToDp(density),
            insets.getTop(density).pxToDp(density),
            insets.getRight(density, layoutDirection = LayoutDirection.Ltr).pxToDp(density),
            insets.getBottom(density).pxToDp(density),
        )
        return result
    }

    // TODO PADS... This can be optimized by caching the latest settings here to avoid going trough storage and serialization.
    fun observeSettings(
        touchControllerID: TouchControllerID,
        orientation: Orientation,
        density: Density,
        insets: WindowInsets
    ): Flow<Settings> {
        val paddings = computeInsetsPaddings(density, insets)
        val horizontalPadding = max(
            paddings.calculateLeftPadding(LayoutDirection.Ltr),
            paddings.calculateRightPadding(LayoutDirection.Ltr)
        )
        val verticalPadding = paddings.calculateBottomPadding()
        val defaultSettings = Settings(
            scale = DEFAULT_SCALE,
            rotation = DEFAULT_ROTATION,
            marginX = horizontalPadding.value / MAX_MARGINS,
            marginY = verticalPadding.value / MAX_MARGINS,
        )
        return FlowSharedPreferences(sharedPreferences).getString(getPreferenceString(touchControllerID, orientation))
            .asFlow()
            .map {
                if (it.isBlank()) {
                    defaultSettings
                } else {
                    Json.decodeFromString(Settings.serializer(), it)
                }
            }
    }

    suspend fun storeSettings(touchControllerID: TouchControllerID, orientation: Orientation, settings: Settings) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit {
                putString(
                    getPreferenceString(touchControllerID, orientation),
                    Json.encodeToString(Settings.serializer(), settings)
                )
            }
        }
    }

    suspend fun resetSettings(touchControllerID: TouchControllerID, orientation: Orientation) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit {
                remove(getPreferenceString(touchControllerID, orientation))
            }
        }
    }

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

    private fun getPreferenceString(
        controllerID: TouchControllerID,
        orientation: Orientation,
    ): String {
        return "touch_controller_settings_${controllerID}_${orientation.ordinal}"
    }
}
