package come.asg.ominoy.app.mobile.feature.gamemenu

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alorma.compose.settings.storage.memory.rememberMemoryBooleanSettingState
import com.alorma.compose.settings.storage.memory.rememberMemoryIntSettingState
import come.asg.ominoy.R
import come.asg.ominoy.app.mobile.feature.gamemenu.tilt.TiltConfigurationMenuEntry
import come.asg.ominoy.app.shared.GameMenuContract
import come.asg.ominoy.app.utils.android.settings.OminoySettingsList
import come.asg.ominoy.app.utils.android.settings.OminoySettingsMenuLink
import come.asg.ominoy.app.utils.android.settings.OminoySettingsSwitch
import kotlin.reflect.KFunction1

@Composable
fun GameMenuHomeScreen(
    navController: NavController,
    gameMenuRequest: GameMenuActivity.GameMenuRequest,
    onResult: KFunction1<Intent.() -> Unit, Unit>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MenuSection(title = stringResource(R.string.game_menu_section_progress)) {
            if (gameMenuRequest.coreConfig.statesSupported) {
                OminoySettingsMenuLink(
                    title = { Text(stringResource(R.string.game_menu_save)) },
                    icon = { MenuIcon(painterResource(R.drawable.ic_menu_save), R.string.game_menu_save) },
                    onClick = { navController.navigateToRoute(GameMenuRoute.SAVE) },
                )
                OminoySettingsMenuLink(
                    title = { Text(stringResource(R.string.game_menu_load)) },
                    icon = { MenuIcon(painterResource(R.drawable.ic_menu_load), R.string.game_menu_load) },
                    onClick = { navController.navigateToRoute(GameMenuRoute.LOAD) },
                )
            }
            if (gameMenuRequest.game.systemId in setOf("gb", "gbc", "gba")) {
                OminoySettingsMenuLink(
                    title = { Text(stringResource(R.string.game_menu_achievements)) },
                    subtitle = { Text(stringResource(R.string.game_menu_achievements_subtitle)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = stringResource(R.string.game_menu_achievements),
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                    },
                    onClick = { navController.navigateToRoute(GameMenuRoute.ACHIEVEMENTS) },
                )
            }
        }

        MenuSection(title = stringResource(R.string.game_menu_section_gameplay)) {
            OminoySettingsMenuLink(
                title = { Text(stringResource(R.string.game_menu_quit)) },
                icon = { MenuIcon(painterResource(R.drawable.ic_menu_quit), R.string.game_menu_quit) },
                onClick = { onResult { putExtra(GameMenuContract.RESULT_QUIT, true) } },
            )
            OminoySettingsMenuLink(
                title = { Text(stringResource(R.string.game_menu_restart)) },
                icon = { MenuIcon(painterResource(R.drawable.ic_menu_restart), R.string.game_menu_restart) },
                onClick = { onResult { putExtra(GameMenuContract.RESULT_RESET, true) } },
            )
            OminoySettingsSwitch(
                title = { Text(stringResource(R.string.game_menu_mute_audio)) },
                icon = { MenuIcon(painterResource(R.drawable.ic_menu_mute), R.string.game_menu_mute_audio) },
                state = rememberMemoryBooleanSettingState(!gameMenuRequest.audioEnabled),
                onCheckedChange = { onResult { putExtra(GameMenuContract.RESULT_ENABLE_AUDIO, !it) } },
            )
            if (gameMenuRequest.fastForwardSupported) {
                OminoySettingsSwitch(
                    title = { Text(stringResource(R.string.game_menu_fast_forward)) },
                    icon = {
                        MenuIcon(
                            painterResource(R.drawable.ic_menu_fast_forward),
                            R.string.game_menu_fast_forward,
                        )
                    },
                    state = rememberMemoryBooleanSettingState(gameMenuRequest.fastForwardEnabled),
                    onCheckedChange = { onResult { putExtra(GameMenuContract.RESULT_ENABLE_FAST_FORWARD, it) } },
                )
            }
            if (gameMenuRequest.numDisks > 1) {
                OminoySettingsList(
                    title = { Text(stringResource(R.string.game_menu_change_disk_button)) },
                    items =
                        (1..gameMenuRequest.numDisks).map {
                            stringResource(
                                R.string.game_menu_change_disk_disk,
                                it,
                            )
                        },
                    useSelectedValueAsSubtitle = false,
                    icon = {
                        MenuIcon(
                            painterResource(R.drawable.ic_menu_disk),
                            R.string.game_menu_change_disk_button,
                        )
                    },
                    state = rememberMemoryIntSettingState(gameMenuRequest.currentDisk),
                    onItemSelected = { index, _ -> onResult { putExtra(GameMenuContract.RESULT_CHANGE_DISK, index) } },
                )
            }
        }

        MenuSection(title = stringResource(R.string.game_menu_section_tools)) {
            OminoySettingsMenuLink(
                title = { Text(stringResource(R.string.game_menu_edit_touch_controls)) },
                icon = {
                    MenuIcon(
                        painterResource(R.drawable.ic_menu_controls),
                        R.string.game_menu_edit_touch_controls,
                    )
                },
                onClick = { onResult { putExtra(GameMenuContract.RESULT_EDIT_TOUCH_CONTROLS, true) } },
            )
            OminoySettingsMenuLink(
                title = { Text(stringResource(R.string.game_menu_cheats)) },
                icon = { Icon(Icons.Default.Code, stringResource(R.string.game_menu_cheats)) },
                onClick = { navController.navigateToRoute(GameMenuRoute.CHEATS) },
            )
            if (gameMenuRequest.advancedCoreOptions.isNotEmpty() || gameMenuRequest.coreOptions.isNotEmpty()) {
                OminoySettingsMenuLink(
                    title = { Text(stringResource(R.string.game_menu_settings)) },
                    icon = { MenuIcon(painterResource(R.drawable.ic_menu_settings), R.string.game_menu_settings) },
                    onClick = { navController.navigateToRoute(GameMenuRoute.OPTIONS) },
                )
            }
            if (gameMenuRequest.allTiltConfigurations.isNotEmpty()) {
                val tiltConfigurationEntries =
                    gameMenuRequest.allTiltConfigurations.map {
                        TiltConfigurationMenuEntry.fromTiltConfiguration(it)
                    }
                val selectedIndex =
                    gameMenuRequest.allTiltConfigurations.indexOf(
                        gameMenuRequest.currentTiltConfiguration,
                    )
                OminoySettingsList(
                    title = { Text(stringResource(R.string.game_menu_tilt_sensor)) },
                    items = tiltConfigurationEntries.map { stringResource(it.descriptionId) },
                    useSelectedValueAsSubtitle = false,
                    icon = { Icon(Icons.Default.Sensors, stringResource(R.string.game_menu_tilt_sensor)) },
                    state = rememberMemoryIntSettingState(selectedIndex),
                    onItemSelected = { index, _ ->
                        onResult {
                            putExtra(
                                GameMenuContract.RESULT_CHANGE_TILT_CONFIG,
                                tiltConfigurationEntries[index].configuration,
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun MenuSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)),
    ) {
        Column {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 4.dp),
            )
            content()
        }
    }
}

@Composable
private fun MenuIcon(
    painter: androidx.compose.ui.graphics.painter.Painter,
    descriptionId: Int,
) {
    Icon(
        painter = painter,
        contentDescription = stringResource(descriptionId),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
