package com.swordfish.touchinput.radial.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Appearance
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Orientation
import com.swordfish.touchinput.radial.settings.TouchControllerSettingsManager.Settings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TouchControlAppearanceSettingsTest {
    private lateinit var preferences: SharedPreferences

    @Before
    fun setUp() {
        preferences = RuntimeEnvironment.getApplication().getSharedPreferences("controls", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
    }

    @Test
    fun `old layouts keep their size and receive neutral defaults`() =
        runBlocking {
            preferences.edit().putString(
                "touch_controller_settings_GBA_0",
                """{"scale":0.3,"rotation":0.2,"marginX":0.4,"marginY":0.1}""",
            ).commit()

            assertEquals(Settings(scale = 0.3f, rotation = 0.2f, marginX = 0.4f, marginY = 0.1f), read(manager()))
        }

    @Test
    fun `custom appearance survives manager recreation`() =
        runBlocking {
            val settings =
                Settings(
                    scale = 0.2f,
                    opacity = 0.4f,
                    transparentBackground = true,
                )
            manager().storeSettings(TouchControllerID.GBA, Orientation.PORTRAIT, settings)

            assertEquals(settings, read(manager()))
        }

    @Test
    fun `systems sharing a controller can be customized separately`() =
        runBlocking {
            val gb = manager("gb")
            val gbc = manager("gbc")
            gb.storeSettings(TouchControllerID.GB, Orientation.PORTRAIT, Settings(opacity = 0.6f))

            assertEquals(0.6f, read(gb, TouchControllerID.GB).opacity)
            assertEquals(1f, read(gbc, TouchControllerID.GB).opacity)
        }

    @Test
    fun `orientation and controller layouts stay separate`() =
        runBlocking {
            val manager = manager()
            manager.storeSettings(TouchControllerID.GBA, Orientation.PORTRAIT, Settings(opacity = 0.2f))

            assertEquals(1f, read(manager, orientation = Orientation.LANDSCAPE).opacity)
            assertEquals(1f, read(manager, controller = TouchControllerID.PSP).opacity)
        }

    @Test
    fun `shared appearance updates active observers`() =
        runBlocking {
            val manager = manager()
            val started = CompletableDeferred<Unit>()
            val updated =
                async {
                    withTimeout(5000) {
                        manager.observeSettings(
                            TouchControllerID.GBA,
                            Orientation.PORTRAIT,
                            Density(1f),
                            WindowInsets(0),
                        )
                            .onEach { started.complete(Unit) }
                            .first { it.opacity == 0.4f }
                    }
                }
            started.await()
            manager("psp").storeAppearance(Appearance(scale = 0.8f, opacity = 0.4f, transparentBackground = true))
            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(0.8f, updated.await().scale)
            assertTrue(updated.await().transparentBackground)
        }

    @Test
    fun `shared appearance preserves local geometry`() =
        runBlocking {
            val manager = manager()
            manager.storeSettings(
                TouchControllerID.GBA,
                Orientation.PORTRAIT,
                Settings(marginX = 0.7f, marginY = 0.2f, rotation = 0.3f, useSharedAppearance = true),
            )
            manager.storeAppearance(Appearance(opacity = 0.3f))
            val settings = read(manager)

            assertEquals(0.3f, settings.opacity)
            assertEquals(0.7f, settings.marginX)
            assertEquals(0.2f, settings.marginY)
            assertEquals(0.3f, settings.rotation)
        }

    @Test
    fun `shared changes leave custom appearance alone`() =
        runBlocking {
            val manager = manager()
            manager.storeSettings(TouchControllerID.GBA, Orientation.PORTRAIT, Settings(opacity = 0.6f))
            manager.storeAppearance(Appearance(opacity = 0.2f, transparentBackground = true))

            assertEquals(0.6f, read(manager).opacity)
            assertFalse(read(manager).transparentBackground)
        }

    @Test
    fun `reset does not restore legacy settings or reset other systems`() =
        runBlocking {
            preferences.edit().putString("touch_controller_settings_GB_0", """{"scale":0.2}""").commit()
            val gb = manager("gb")
            gb.resetSettings(TouchControllerID.GB, Orientation.PORTRAIT)

            assertEquals(0.5f, read(manager("gb"), TouchControllerID.GB).scale)
            assertTrue(read(manager("gb"), TouchControllerID.GB).useSharedAppearance)
            assertEquals(0.2f, read(manager("gbc"), TouchControllerID.GB).scale)
        }

    @Test
    fun `reset restores safe insets and shared appearance`() =
        runBlocking {
            val manager = manager()
            manager.storeSettings(
                TouchControllerID.GBA,
                Orientation.PORTRAIT,
                Settings(marginX = 0.8f, rotation = 0.4f),
            )
            manager.storeAppearance(Appearance(opacity = 0.6f))
            manager.resetSettings(TouchControllerID.GBA, Orientation.PORTRAIT)
            val settings =
                manager.observeSettings(
                    TouchControllerID.GBA,
                    Orientation.PORTRAIT,
                    Density(2f),
                    WindowInsets(48, 0, 0, 96),
                ).first()

            assertEquals(0.25f, settings.marginX)
            assertEquals(0.5f, settings.marginY)
            assertEquals(0f, settings.rotation)
            assertEquals(0.6f, settings.opacity)
        }

    @Test
    fun `unreadable settings fall back to defaults`() =
        runBlocking {
            preferences.edit().putString("touch_controller_settings_gba_GBA_0", "invalid json").commit()
            assertTrue(read(manager()).useSharedAppearance)
        }

    @Test
    fun `unknown settings do not discard the saved layout`() =
        runBlocking {
            preferences.edit().putString(
                "touch_controller_settings_gba_GBA_0",
                """{"scale":0.3,"futureSetting":true}""",
            ).commit()
            assertEquals(0.3f, read(manager()).scale)
        }

    private fun manager(systemId: String = "gba") = TouchControllerSettingsManager(preferences, systemId)

    private suspend fun read(
        manager: TouchControllerSettingsManager,
        controller: TouchControllerID = TouchControllerID.GBA,
        orientation: Orientation = Orientation.PORTRAIT,
    ) = manager.observeSettings(controller, orientation, Density(1f), WindowInsets(0)).first()
}
