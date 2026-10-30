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
class TouchControlGroupSizeSettingsTest {
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
            assertEquals(1f, settings.leftScale)
            assertEquals(1f, settings.rightScale)
        }

    @Test
    fun `custom settings survive manager recreation`() =
        runBlocking {
            val settings = Settings(leftScale = 0.7f, rightScale = 1.3f)
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
                Settings(leftScale = 0.7f, rightScale = 1.3f),
            )
            assertEquals(1f, read(manager, orientation = Orientation.LANDSCAPE).leftScale)
            assertEquals(1f, read(manager, controller = TouchControllerID.PSP).rightScale)
        }

    @Test
    fun `reset restores neutral group sizes`() =
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
                Settings(leftScale = 0.7f, rightScale = 1.3f),
            )
            manager.resetSettings(TouchControllerID.GBA, Orientation.PORTRAIT)
            assertEquals(1f, settings.first().leftScale)
            assertEquals(1f, settings.first().rightScale)
            assertEquals(1f, read(manager()).leftScale)
            assertEquals(1f, read(manager()).rightScale)
        }

    private fun manager() = TouchControllerSettingsManager(preferences)

    private suspend fun read(
        manager: TouchControllerSettingsManager,
        controller: TouchControllerID = TouchControllerID.GBA,
        orientation: Orientation = Orientation.PORTRAIT,
    ) = manager.observeSettings(controller, orientation, Density(1f), WindowInsets(0)).first()
}
