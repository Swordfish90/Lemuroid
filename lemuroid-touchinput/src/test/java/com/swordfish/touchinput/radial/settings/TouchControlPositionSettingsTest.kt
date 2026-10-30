package com.swordfish.touchinput.radial.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Orientation
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TouchControlPositionSettingsTest {
    private lateinit var preferences: SharedPreferences

    @Before
    fun setUp() {
        preferences = RuntimeEnvironment.getApplication().getSharedPreferences("controls", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
    }

    @Test
    fun `legacy settings keep their geometry`() =
        runBlocking {
            preferences.edit().putString("touch_controller_settings_GBA_0", """{"scale":0.3,"marginY":0.2}""").commit()
            val settings = read(manager())
            assertEquals(0.3f, settings.scale)
            assertEquals(0.2f, settings.marginY)
            assertEquals(0f, settings.leftOffsetX)
            assertEquals(0f, settings.rightOffsetY)
        }

    @Test
    fun `custom settings survive manager recreation`() =
        runBlocking {
            val settings = Settings(leftOffsetX = 0.1f, leftOffsetY = -0.2f, rightOffsetX = -0.1f, rightOffsetY = -0.3f)
            manager().storeSettings(TouchControllerID.GBA, Orientation.PORTRAIT, settings)
            assertEquals(settings, read(manager()))
        }

    @Test
    fun `other layouts and orientations are unchanged`() =
        runBlocking {
            val manager = manager()
            manager.storeSettings(
                TouchControllerID.GBA,
                Orientation.PORTRAIT,
                Settings(leftOffsetX = 0.1f, leftOffsetY = -0.2f, rightOffsetX = -0.1f, rightOffsetY = -0.3f),
            )
            assertEquals(0f, read(manager, orientation = Orientation.LANDSCAPE).leftOffsetX)
            assertEquals(0f, read(manager, controller = TouchControllerID.PSP).rightOffsetY)
        }

    @Test
    fun `reset restores saved positions`() =
        runBlocking {
            val manager = manager()
            val settings =
                manager.observeSettings(
                    TouchControllerID.GBA,
                    Orientation.PORTRAIT,
                    Density(1f),
                    WindowInsets(0),
                )
            settings.first()
            manager.storeSettings(
                TouchControllerID.GBA,
                Orientation.PORTRAIT,
                Settings(leftOffsetX = 0.1f, leftOffsetY = -0.2f, rightOffsetX = -0.1f, rightOffsetY = -0.3f),
            )
            manager.resetSettings(TouchControllerID.GBA, Orientation.PORTRAIT)
            assertEquals(0f, settings.first().leftOffsetX)
            assertEquals(0f, settings.first().rightOffsetY)
            assertEquals(0f, read(manager()).leftOffsetX)
            assertEquals(0f, read(manager()).rightOffsetY)
        }

    private fun manager() = TouchControllerSettingsManager(preferences)

    private suspend fun read(
        manager: TouchControllerSettingsManager,
        controller: TouchControllerID = TouchControllerID.GBA,
        orientation: Orientation = Orientation.PORTRAIT,
    ) = manager.observeSettings(controller, orientation, Density(1f), WindowInsets(0)).first()
}
