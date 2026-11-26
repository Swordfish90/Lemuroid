package com.swordfish.lemuroid.app.mobile.feature.gamemenu.coreoptions

import android.content.Context
import android.content.Intent
import androidx.compose.ui.res.painterResource
import com.alorma.compose.settings.storage.memory.rememberMemoryBooleanSettingState
import com.swordfish.lemuroid.app.shared.GameMenuContract
import kotlin.reflect.KFunction1
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.CustomShaderLoader
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.gamemenu.GameMenuActivity
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOptionsPreferenceHelper
import com.swordfish.lemuroid.app.shared.coreoptions.LemuroidCoreOption
import com.swordfish.lemuroid.app.shared.settings.ControllerConfigsManager
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsGroup
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsList
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsSwitch
import com.swordfish.lemuroid.app.utils.android.settings.booleanPreferenceState
import com.swordfish.lemuroid.app.utils.android.settings.indexPreferenceState
import com.swordfish.lemuroid.lib.core.CoreVariablesManager

@Composable
fun GameMenuCoreOptionsScreen(
    viewModel: GameMenuCoreOptionsViewModel,
    gameMenuRequest: GameMenuActivity.GameMenuRequest,
    onResult: KFunction1<Intent.() -> Unit, Unit>,
) {
    val context = LocalContext.current

    val connectedGamePads by viewModel.connectedGamePads.collectAsState(0)

    val allOptions =
        remember(gameMenuRequest) {
            gameMenuRequest.coreOptions + gameMenuRequest.advancedCoreOptions
        }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        var hasCS by remember { mutableStateOf(CustomShaderLoader.hasCustomShader(context)) }; var rev by remember { mutableIntStateOf(0) }
        val passDefs = remember(hasCS, rev) { if (hasCS) CustomShaderLoader.getPassDefs(context) else emptyList() }; val shaderName = remember(hasCS, rev) { if (hasCS) CustomShaderLoader.getShaderName(context) else "" }
        val pk = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if(u!=null){ val ok=CustomShaderLoader.saveCustomShaderZip(context,u); hasCS=ok; rev++; Toast.makeText(context, if(ok) "Custom shader loaded!" else "Invalid shader zip", Toast.LENGTH_SHORT).show() } }
        var showShaderInfo by remember { mutableStateOf(false) }; if (showShaderInfo) { AlertDialog(onDismissRequest = { showShaderInfo = false }, shape = RoundedCornerShape(20.dp), title = { Text("Custom Shader") }, text = { Text("Shader functionality is still a work in progress, some may work, while others will not. All shaders tested so far have been from mGBA.") }, confirmButton = { TextButton(onClick = { showShaderInfo = false }) { Text("OK") } }) }; val showCS = gameMenuRequest.game.systemId.uppercase() in setOf("SNES", "GBA", "NES", "GB", "GBC", "NDS", "GENESIS", "MD"); if (showCS) LemuroidSettingsGroup(title = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Text("Custom Shader"); IconButton(onClick = { showShaderInfo = true }, modifier = Modifier.size(24.dp)) { Icon(imageVector = Icons.Outlined.Info, contentDescription = "Custom Shader Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) } } }) {
            LemuroidSettingsMenuLink(title = { Text("Load Custom Shader (.zip)") }, subtitle = { Text(if(hasCS) "Active: $shaderName (${passDefs.size} passes)" else "Select Retro Shader zip") }, onClick = { pk.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) })
            if(hasCS){
                LemuroidSettingsMenuLink(title = { Text("Clear Custom Shader") }, onClick = { CustomShaderLoader.clearCustomShader(context); hasCS=false; rev++ })
                LaunchedEffect(passDefs.size) { if (passDefs.isNotEmpty()) CustomShaderLoader.setMaxPasses(context, passDefs.size) }; Text(text = "Active Passes ${passDefs.size}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                passDefs.forEach { pd ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text(text = pd.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); if (pd.name == "Preprocessing") { OutlinedButton(onClick = { CustomShaderLoader.resetPreprocessing(context); rev++ }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)) { Text("Restore Defaults", style = MaterialTheme.typography.labelSmall) } } }
                    if (pd.uniforms.isEmpty()) { Text(text = "No adjustable parameters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) }
                    pd.uniforms.forEach { ud -> for (comp in ud.defaults.indices) {
                        val compLabel = if (ud.labels.size > comp) "${ud.friendlyName} (${ud.labels[
comp
]})" else ud.friendlyName
                        var uVal by remember(pd.index, ud.name, comp, rev) { mutableFloatStateOf(CustomShaderLoader.getCompVal(context, pd.index, ud.name, comp, ud.defaults[
comp
])) }
                        val curVal by rememberUpdatedState(uVal); val decInt = remember { MutableInteractionSource() }; val incInt = remember { MutableInteractionSource() }
                        val decPressed by decInt.collectIsPressedAsState(); val incPressed by incInt.collectIsPressedAsState()
                        LaunchedEffect(decPressed) { if (decPressed) { var nxt = (((curVal - 0.002f) * 1000f).roundToInt() / 1000f).coerceIn(ud.minVal, ud.maxVal); uVal = nxt; CustomShaderLoader.setCompVal(context, pd.index, ud.name, comp, nxt); delay(350L); while (true) { nxt = (((curVal - 0.002f) * 1000f).roundToInt() / 1000f).coerceIn(ud.minVal, ud.maxVal); uVal = nxt; CustomShaderLoader.setCompVal(context, pd.index, ud.name, comp, nxt); delay(45L) } } }
                        LaunchedEffect(incPressed) { if (incPressed) { var nxt = (((curVal + 0.002f) * 1000f).roundToInt() / 1000f).coerceIn(ud.minVal, ud.maxVal); uVal = nxt; CustomShaderLoader.setCompVal(context, pd.index, ud.name, comp, nxt); delay(350L); while (true) { nxt = (((curVal + 0.002f) * 1000f).roundToInt() / 1000f).coerceIn(ud.minVal, ud.maxVal); uVal = nxt; CustomShaderLoader.setCompVal(context, pd.index, ud.name, comp, nxt); delay(45L) } } }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) {
                            Text(text = compLabel, style = MaterialTheme.typography.bodySmall, maxLines = 1, modifier = Modifier.width(96.dp).basicMarquee(iterations = Int.MAX_VALUE, velocity = 24.dp))
                                OutlinedButton(onClick = {}, interactionSource = decInt, contentPadding = PaddingValues(0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.size(28.dp)) { Icon(Icons.Default.KeyboardArrowLeft, "Decrease", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
                                Slider(value = uVal.coerceIn(ud.minVal, ud.maxVal), valueRange = ud.minVal..ud.maxVal, modifier = Modifier.weight(1f), onValueChange = { var r = (it * 1000f).roundToInt() / 1000f; val snap = (r * 100f).roundToInt() / 100f; val s10=(r*10f).roundToInt()/10f; val s100=(r*100f).roundToInt()/100f; val s1000=(r*1000f).roundToInt()/1000f; r=when{kotlin.math.abs(r-1f)<=0.025f->1f;kotlin.math.abs(r-s10)<=0.014f->s10;kotlin.math.abs(r-s100)<=0.008f->s100;kotlin.math.abs(r-s1000)<=0.0012f->s1000;else->r}.coerceIn(ud.minVal,ud.maxVal); uVal = r; CustomShaderLoader.setCompVal(context, pd.index, ud.name, comp, r) })
                                OutlinedButton(onClick = {}, interactionSource = incInt, contentPadding = PaddingValues(0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.size(28.dp)) { Icon(Icons.Default.KeyboardArrowRight, "Increase", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }; Text(text = String.format("%.3f", uVal), style = MaterialTheme.typography.bodySmall, maxLines = 1, modifier = Modifier.width(42.dp))
                        }
                    } }
                }
            }
        }
        CoreOptions(gameMenuRequest, allOptions, context, onResult)
        ControllersOptions(gameMenuRequest, maxOf(1, connectedGamePads), context)
    }
}

@Composable
private fun CoreOptions(
    gameMenuRequest: GameMenuActivity.GameMenuRequest,
    coreOptions: List<LemuroidCoreOption>,
    context: Context,
    onResult: KFunction1<Intent.() -> Unit, Unit>,
) {
    val systemID = gameMenuRequest.game.systemId
    if (coreOptions.isEmpty()) {
        return
    }
    var showSgbInfo by remember { mutableStateOf(false) }
    if (showSgbInfo) {
        AlertDialog(onDismissRequest = { showSgbInfo = false }, shape = RoundedCornerShape(20.dp), title = { Text("Super Game Boy Borders") }, text = { Text("To use the Super Game Boy borders, you must select Super Game Boy as the Hardware Model, and then quit the game, and reload it to see the borders. Not all games were made with borders to display.") }, confirmButton = { TextButton(onClick = { showSgbInfo = false }) { Text("OK") } })
    }

    for (coreOption in coreOptions) {
        val k = coreOption.getKey(); val sys = systemID.uppercase(); if (k == "mgba_gb_colors_preset") continue; if (k == "mgba_sgb_borders" && sys != "GB") continue; if (k == "mgba_gb_colors" && sys != "GB" && sys != "GBC") continue; if (k == "mgba_gb_model" && sys == "GBA") continue;
        if (coreOption.getKey() == "mgba_frameskip_threshold" && gameMenuRequest.fastForwardSupported) {
            LemuroidSettingsSwitch(
                title = { Text(text = stringResource(id = R.string.game_menu_fast_forward)) },
                icon = { Icon(painterResource(R.drawable.ic_menu_fast_forward), contentDescription = stringResource(id = R.string.game_menu_fast_forward)) },
                state = rememberMemoryBooleanSettingState(gameMenuRequest.fastForwardEnabled),
                onCheckedChange = { onResult { putExtra(GameMenuContract.RESULT_ENABLE_FAST_FORWARD, it) } },
            )
        }
        if (coreOption.getEntriesValues().toSet() == CoreOptionsPreferenceHelper.BOOLEAN_SET) {
            LemuroidSettingsSwitch(
                state =
                    booleanPreferenceState(
                        CoreVariablesManager.computeSharedPreferenceKey(coreOption.getKey(), systemID),
                        coreOption.getCurrentValue() == "enabled",
                    ),
                title = { if (k == "mgba_sgb_borders") { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Text(text = coreOption.getDisplayName(context)); IconButton(onClick = { showSgbInfo = true }, modifier = Modifier.size(24.dp)) { Icon(imageVector = Icons.Outlined.Info, contentDescription = "Super Game Boy Borders Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) } } } else { Text(text = coreOption.getDisplayName(context)) } },
            )
        } else {
            LemuroidSettingsList(
                title = { if (k == "mgba_sgb_borders") { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Text(text = coreOption.getDisplayName(context)); IconButton(onClick = { showSgbInfo = true }, modifier = Modifier.size(24.dp)) { Icon(imageVector = Icons.Outlined.Info, contentDescription = "Super Game Boy Borders Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) } } } else { Text(text = coreOption.getDisplayName(context)) } },
                items = coreOption.getEntries(context),
                state =
                    indexPreferenceState(
                        CoreVariablesManager.computeSharedPreferenceKey(coreOption.getKey(), systemID),
                        coreOption.getEntriesValues().first(),
                        coreOption.getEntriesValues(),
                    ),
            )
        }
    }
}

@Composable
private fun ControllersOptions(
    gameMenuRequest: GameMenuActivity.GameMenuRequest,
    connectedGamePads: Int,
    context: Context,
) {
    val controllers = gameMenuRequest.coreConfig.controllerConfigs

    val visibleControllers =
        (0 until connectedGamePads)
            .map { it to controllers[it] }
            .filter { (_, controllers) -> controllers != null && controllers.size >= 2 }

    if (visibleControllers.isEmpty()) {
        return
    }

    LemuroidSettingsGroup(
        title = { Text(text = stringResource(R.string.core_settings_category_controllers)) },
    ) {
        visibleControllers.forEach { (port, controllerConfigs) ->
            LemuroidSettingsList(
                title = { Text(text = context.getString(R.string.core_settings_controller, (port + 1).toString())) },
                items = controllerConfigs!!.map { stringResource(id = it.displayName) },
                state =
                    indexPreferenceState(
                        ControllerConfigsManager.getSharedPreferencesId(
                            gameMenuRequest.game.systemId,
                            gameMenuRequest.coreConfig.coreID,
                            port,
                        ),
                        controllerConfigs.map { it.name }.first(),
                        controllerConfigs.map { it.name },
                    ),
            )
        }
    }
}
