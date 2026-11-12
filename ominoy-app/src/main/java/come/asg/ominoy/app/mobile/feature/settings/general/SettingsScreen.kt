package come.asg.ominoy.app.mobile.feature.settings.general

import androidx.compose.foundation.layout.statusBarsPadding
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import come.asg.ominoy.R
import come.asg.ominoy.app.mobile.feature.main.MainRoute
import come.asg.ominoy.app.mobile.feature.main.navigateToRoute
import come.asg.ominoy.app.mobile.feature.settings.retroachievements.RetroAchievementsAccountBridge
import come.asg.ominoy.app.shared.library.LibraryIndexScheduler
import come.asg.ominoy.app.shared.settings.DataFolderPickerLauncher
import come.asg.ominoy.app.utils.android.settings.OminoyCardSettingsGroup
import come.asg.ominoy.app.utils.android.settings.OminoySettingsList
import come.asg.ominoy.app.utils.android.settings.OminoySettingsMenuLink
import come.asg.ominoy.app.utils.android.settings.OminoySettingsPage
import come.asg.ominoy.app.mobile.shared.compose.ui.OminoyHomeColors as HomeColors
import come.asg.ominoy.app.utils.android.settings.OminoySettingsSlider
import come.asg.ominoy.app.utils.android.settings.OminoySettingsSwitch
import come.asg.ominoy.app.utils.android.settings.TouchButtonAppearanceSettings
import come.asg.ominoy.app.utils.android.settings.booleanPreferenceState
import come.asg.ominoy.app.utils.android.settings.indexPreferenceState
import come.asg.ominoy.app.utils.android.settings.intPreferenceState
import come.asg.ominoy.app.utils.android.stringListResource

private val OminoyTeal = Color(0xFF8AEBDD)
private val OminoySurface = Color(0xFF1B1A23)
private val OminoyLine = Color.White.copy(alpha = 0.08f)
private val OminoyMuted = Color(0xFF9D98AB)

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    navController: NavController,
) {
    val state = viewModel.uiState.collectAsState(SettingsViewModel.State()).value
    val scanInProgress = viewModel.directoryScanInProgress.collectAsState(false).value
    val indexingInProgress = viewModel.indexingInProgress.collectAsState(false).value
    val retroAchievementsAccount by RetroAchievementsAccountBridge.credentials.collectAsState()
    var selectedSection by rememberSaveable { mutableStateOf<String?>(null) }

    if (selectedSection == null) {
        SettingsOverview(
            modifier = modifier,
            onOpenRetroAchievements = { navController.navigateToRoute(MainRoute.SETTINGS_RETROACHIEVEMENTS) },
            onOpenSection = { selectedSection = it },
            retroAchievementsUsername = retroAchievementsAccount?.username,
        )
    } else {
        OminoySettingsPage(modifier = modifier) {
            SettingsDetailHeader(title = selectedSection!!, onBack = { selectedSection = null })
            when (selectedSection) {
                "Library" -> RomsSettings(
                    state = state,
                    onChangeFolder = { viewModel.changeLocalStorageFolder() },
                    indexingInProgress = indexingInProgress,
                    scanInProgress = scanInProgress,
                )
                "General" -> GeneralSettings(navController = navController)
                "Display" -> DisplaySettings(navController = navController)
                "Input" -> InputSettings(navController = navController)
                "Advanced" -> MiscSettings(isSaveSyncSupported = state.isSaveSyncSupported, navController = navController)
            }
        }
    }
}

@Composable
private fun SettingsOverview(
    modifier: Modifier,
    onOpenRetroAchievements: () -> Unit,
    onOpenSection: (String) -> Unit,
    retroAchievementsUsername: String?,
) {
    Box(modifier = modifier.fillMaxSize().background(HomeColors.Background)) {
        Box(Modifier.align(Alignment.TopStart).padding(start = 8.dp, top = 20.dp).size(360.dp).clip(CircleShape).background(Brush.radialGradient(listOf(HomeColors.PurpleGlow.copy(alpha = .34f), Color.Transparent))))
        Box(Modifier.align(Alignment.TopEnd).padding(top = 240.dp, end = 4.dp).size(320.dp).clip(CircleShape).background(Brush.radialGradient(listOf(HomeColors.TealGlow.copy(alpha = .25f), Color.Transparent))))
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 18.dp, end = 18.dp, top = 40.dp, bottom = 144.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("YOUR PLAYER", color = HomeColors.Mint, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                Text("Settings", color = HomeColors.Text, fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.Black, letterSpacing = (-1.2).sp)
                Text("Tune your library, controls, and game experience.", color = HomeColors.Muted, fontSize = 14.sp)
            }
            SettingsFeatureCard(
                title = "RetroAchievements",
                subtitle = retroAchievementsUsername?.let { "$it · View your achievement progress" } ?: "Connect your account to track achievements",
                connected = retroAchievementsUsername != null,
                onClick = onOpenRetroAchievements,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsSectionLabel("YOUR SETUP")
                SettingsCategoryGroup {
                    SettingsCategoryRow("Library", "ROMs, save files and folders") { onOpenSection("Library") }
                    SettingsRowDivider()
                    SettingsCategoryRow("General", "Autosave, theme and HD mode") { onOpenSection("General") }
                    SettingsRowDivider()
                    SettingsCategoryRow("Display", "Shader filters and viewport") { onOpenSection("Display") }
                    SettingsRowDivider()
                    SettingsCategoryRow("Input", "Touch controls and gamepads") { onOpenSection("Input") }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsSectionLabel("MORE")
                SettingsCategoryGroup {
                    SettingsCategoryRow("Advanced", "Storage, diagnostics and power-user options") { onOpenSection("Advanced") }
                }
            }
            Text("OMINOY  ·  RETRO, READY.", color = HomeColors.Muted.copy(alpha = .72f), fontSize = 10.sp, letterSpacing = 1.7.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun SettingsFeatureCard(title: String, subtitle: String, connected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF2A1F45), Color(0xFF201A2B), Color(0xFF0F3A3D))))
            .clickable(onClick = onClick).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(HomeColors.Mint.copy(alpha = .16f)), contentAlignment = Alignment.Center) {
            Text("★", color = HomeColors.Mint, fontSize = 23.sp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(title, color = HomeColors.Text, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            if (connected) {
                Surface(shape = RoundedCornerShape(99.dp), color = HomeColors.Mint.copy(alpha = .15f)) {
                    Text(
                        "LINKED",
                        color = HomeColors.Mint,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = .6.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    )
                }
            }
            Text(subtitle, color = HomeColors.Text.copy(alpha = .78f), fontSize = 12.sp, lineHeight = 16.sp)
        }
        Text("›", color = HomeColors.Mint, fontSize = 29.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(text, color = HomeColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.7.sp, modifier = Modifier.padding(start = 5.dp))
}

@Composable
private fun SettingsCategoryGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = HomeColors.CardVariant.copy(alpha = .96f),
        border = BorderStroke(1.dp, HomeColors.Line),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsCategoryRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = HomeColors.Text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = HomeColors.Muted, fontSize = 11.sp, lineHeight = 14.sp)
        }
        Text("›", color = HomeColors.Muted, fontSize = 24.sp)
    }
}

@Composable
private fun SettingsRowDivider() {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(HomeColors.Line))
}

@Composable
private fun SettingsDetailHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(HomeColors.SurfaceVariant).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            Text("‹", color = HomeColors.Text, fontSize = 30.sp, lineHeight = 30.sp)
        }
        Column {
            Text("SETTINGS", color = HomeColors.Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text(title, color = HomeColors.Text, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun MiscSettings(isSaveSyncSupported: Boolean, navController: NavController) {
    OminoyCardSettingsGroup(title = { Text(text = stringResource(id = R.string.settings_category_misc)) }) {
        if (isSaveSyncSupported) {
            OminoySettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.settings_title_save_sync)) },
                subtitle = { Text(text = stringResource(id = R.string.settings_description_save_sync)) },
                onClick = { navController.navigateToRoute(MainRoute.SETTINGS_SAVE_SYNC) },
            )
        }
        OminoySettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.settings_title_advanced_settings)) },
            subtitle = { Text(text = stringResource(id = R.string.settings_description_advanced_settings)) },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_ADVANCED) },
        )
    }
}

@Composable
private fun InputSettings(navController: NavController) {
    OminoyCardSettingsGroup(title = { Text(text = stringResource(id = R.string.settings_category_input)) }) {
        OminoySettingsList(
            state = indexPreferenceState(R.string.pref_key_haptic_feedback_mode, "press", stringListResource(R.array.pref_key_haptic_feedback_mode_values)),
            title = { Text(text = "Touch feedback") },
            items = stringListResource(R.array.pref_key_haptic_feedback_mode_display_names),
        )
        OminoySettingsMenuLink(
            title = { Text(text = "Gamepads") },
            subtitle = { Text(text = "Configure connected controllers") },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_INPUT_DEVICES) },
        )
        OminoySettingsMenuLink(
            title = { Text(text = "Touch layout editor") },
            subtitle = { Text(text = "Move and resize on-screen controls") },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_TOUCH_LAYOUT) },
        )
        TouchButtonAppearanceSettings()
    }
}

@Composable
private fun GeneralSettings(navController: NavController) {
    val hdMode = booleanPreferenceState(R.string.pref_key_hd_mode, false)
    val immersiveMode = booleanPreferenceState(R.string.pref_key_enable_immersive_mode, false)
    val amoledTheme = booleanPreferenceState(R.string.pref_key_amoled_theme, false)
    OminoyCardSettingsGroup {
        OminoySettingsSwitch(state = booleanPreferenceState(R.string.pref_key_autosave, true), title = { Text("Autosave") }, subtitle = { Text("Save automatically when leaving a game") })
        OminoySettingsSwitch(state = immersiveMode, title = { Text("Immersive mode") }, subtitle = { Text("Hide system bars while playing") })
        OminoySettingsSwitch(state = amoledTheme, title = { Text("AMOLED theme") }, subtitle = { Text("Pure black backgrounds") })
        OminoySettingsSwitch(state = hdMode, title = { Text("HD mode") }, subtitle = { Text("Higher-resolution rendering when available") })
        OminoySettingsSlider(
            enabled = hdMode.value,
            state = intPreferenceState(stringResource(R.string.pref_key_hd_mode_quality), 2),
            steps = 1,
            valueRange = 0f..2f,
            title = { Text("Render scale") },
            subtitle = { Text("Higher values look sharper but use more power") },
        )
    }
}

@Composable
private fun DisplaySettings(navController: NavController) {
    val hdMode = booleanPreferenceState(R.string.pref_key_hd_mode, false)
    OminoyCardSettingsGroup {
        OminoySettingsList(
            enabled = !hdMode.value,
            state = indexPreferenceState(R.string.pref_key_shader_filter, "auto", stringListResource(R.array.pref_key_shader_filter_values).toList()),
            title = { Text("Shader filter") },
            items = stringListResource(R.array.pref_key_shader_filter_display_names),
        )
        OminoySettingsMenuLink(
            title = { Text("Viewport alignment") },
            subtitle = { Text("Position of the game screen") },
            onClick = { navController.navigateToRoute(MainRoute.SETTINGS_VIEWPORT_ALIGNMENT) },
        )
    }
}

@Composable
private fun RomsSettings(
    state: SettingsViewModel.State,
    onChangeFolder: () -> Unit,
    indexingInProgress: Boolean,
    scanInProgress: Boolean,
) {
    val context = LocalContext.current
    val emptyDirectory = stringResource(R.string.none)
    val currentDirectoryName = remember(state.currentDirectory) {
        runCatching { DocumentFile.fromTreeUri(context, Uri.parse(state.currentDirectory))?.name }.getOrNull() ?: emptyDirectory
    }
    OminoyCardSettingsGroup(title = { Text(text = "Library") }) {
        OminoySettingsMenuLink(title = { Text("ROM directory") }, subtitle = { Text(currentDirectoryName) }, onClick = onChangeFolder, enabled = !indexingInProgress)
        OminoySettingsMenuLink(title = { Text("Data folder") }, subtitle = { Text("ROMs, saves, save states and previews") }, onClick = { DataFolderPickerLauncher.pickFolder(context) })
        if (scanInProgress) {
            OminoySettingsMenuLink(title = { Text("Stop") }, onClick = { LibraryIndexScheduler.cancelLibrarySync(context) })
        } else {
            OminoySettingsMenuLink(title = { Text("Rescan library") }, subtitle = { Text("Look for new games") }, onClick = { LibraryIndexScheduler.scheduleLibrarySync(context) }, enabled = !indexingInProgress)
        }
    }
}
