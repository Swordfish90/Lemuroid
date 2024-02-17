package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.storage.base.rememberIntSettingState
import com.alorma.compose.settings.ui.SettingsList
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameMenuContract
import kotlin.reflect.KFunction1

@Composable
fun GameMenuHomeScreen(
    navController: NavController,
    gameMenuRequest: GameMenuActivity.GameMenuRequest,
    onResult: KFunction1<Intent.() -> Unit, Unit>
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_save)) },
            icon = { Icon(painterResource(R.drawable.ic_menu_save), contentDescription = stringResource(id = R.string.game_menu_save)) },
            onClick = { navController.navigateToRoute(GameMenuRoute.SAVE) }
        )

        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_load)) },
            icon = { Icon(painterResource(R.drawable.ic_menu_load), contentDescription = stringResource(id = R.string.game_menu_load)) },
            onClick = { navController.navigateToRoute(GameMenuRoute.LOAD) }
        )

        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_quit)) },
            icon = { Icon(painterResource(R.drawable.ic_menu_quit), contentDescription = stringResource(id = R.string.game_menu_quit)) },
            onClick = {
                onResult { putExtra(GameMenuContract.RESULT_QUIT, true) }
            }
        )

        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_restart)) },
            icon = { Icon(painterResource(R.drawable.ic_menu_restart), contentDescription = stringResource(id = R.string.game_menu_restart)) },
            onClick = {
                onResult { putExtra(GameMenuContract.RESULT_RESET, true) }
            }
        )

        SettingsSwitch(
            title = { Text(text = stringResource(id = R.string.game_menu_mute_audio)) },
            icon = { Icon(painterResource(R.drawable.ic_menu_mute), contentDescription = stringResource(id = R.string.game_menu_mute_audio)) },
            state = rememberBooleanSettingState(!gameMenuRequest.audioEnabled),
            onCheckedChange = {
                onResult { putExtra(GameMenuContract.RESULT_ENABLE_AUDIO, !it) }
            }
        )

        if (gameMenuRequest.fastForwardSupported) {
            SettingsSwitch(
                title = { Text(text = stringResource(id = R.string.game_menu_fast_forward)) },
                icon = { Icon(painterResource(R.drawable.ic_menu_fast_forward), contentDescription = stringResource(id = R.string.game_menu_fast_forward)) },
                state = rememberBooleanSettingState(gameMenuRequest.fastForwardEnabled),
                onCheckedChange = {
                    onResult { putExtra(GameMenuContract.RESULT_ENABLE_FAST_FORWARD, it) }
                }
            )
        }

        if (gameMenuRequest.numDisks > 1) {
            SettingsList(
                title = { Text(text = stringResource(id = R.string.game_menu_change_disk_button)) },
                items = (1 .. gameMenuRequest.numDisks).map { stringResource(R.string.game_menu_change_disk_disk, it) },
                useSelectedValueAsSubtitle = false,
                icon = { Icon(painterResource(R.drawable.ic_menu_disk), contentDescription = stringResource(id = R.string.game_menu_change_disk_button)) },
                state = rememberIntSettingState(gameMenuRequest.currentDisk),
                onItemSelected = { index, _ ->
                    onResult { putExtra(GameMenuContract.RESULT_CHANGE_DISK, index) }
                }
            )
        }

        SettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.game_menu_edit_touch_controls)) },
            icon = { Icon(painterResource(R.drawable.ic_menu_controls), contentDescription = stringResource(id = R.string.game_menu_edit_touch_controls)) },
            onClick = {
                onResult { putExtra(GameMenuContract.RESULT_EDIT_TOUCH_CONTROLS, true) }
            }
        )


        if (gameMenuRequest.advancedCoreOptions.isNotEmpty() || gameMenuRequest.coreOptions.isNotEmpty()) {
            SettingsMenuLink(
                title = { Text(text = stringResource(id = R.string.game_menu_settings)) },
                icon = {
                    Icon(
                        painterResource(R.drawable.ic_menu_settings),
                        contentDescription = stringResource(id = R.string.game_menu_settings)
                    )
                },
                onClick = { navController.navigateToRoute(GameMenuRoute.OPTIONS) }
            )
        }
    }
}
