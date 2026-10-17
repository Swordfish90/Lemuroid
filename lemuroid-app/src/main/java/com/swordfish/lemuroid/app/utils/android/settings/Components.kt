package com.swordfish.lemuroid.app.utils.android.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.storage.base.SettingValueState
import kotlin.math.roundToInt

// Entries outside a group use the shape of a single standalone item.
private val LocalSettingsItemCount = staticCompositionLocalOf { 1 }

@Composable
fun LemuroidSettingsPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        content()
    }
}

@Composable
fun LemuroidSettingsSwitch(
    index: Int = 0,
    enabled: Boolean = true,
    state: SettingValueState<Boolean>,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    val itemCount = LocalSettingsItemCount.current
    SegmentedListItem(
        onClick = {
            val checked = !state.value
            state.value = checked
            onCheckedChange(checked)
        },
        shapes = ListItemDefaults.segmentedShapes(index, itemCount),
        colors = lemuroidSegmentedSettingsColors(),
        modifier =
            Modifier.semantics {
                role = Role.Switch
                toggleableState = ToggleableState(state.value)
            },
        enabled = enabled,
        leadingContent = icon,
        supportingContent = subtitle,
        trailingContent = { Switch(checked = state.value, onCheckedChange = null, enabled = enabled) },
        content = title,
    )
}

@Composable
fun LemuroidSettingsMenuLink(
    index: Int = 0,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val itemCount = LocalSettingsItemCount.current
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, itemCount),
        colors = lemuroidSegmentedSettingsColors(),
        enabled = enabled,
        leadingContent = icon,
        supportingContent = subtitle,
        trailingContent = action,
        content = title,
    )
}

@Composable
fun LemuroidSettingsGroup(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (title != null) {
            SettingsGroupTitleSmall(title)
        }
        content()
    }
}

/**
 * Groups segmented entries beneath an optional heading.
 *
 * [itemCount] must match the visible entries, whose indices determine their outer corners.
 */
@Composable
fun LemuroidSegmentedSettingsGroup(
    itemCount: Int,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    LemuroidSettingsGroup(
        modifier = modifier.padding(start = 16.dp, end = 16.dp),
        title = title,
    ) {
        CompositionLocalProvider(LocalSettingsItemCount provides itemCount) {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                content()
            }
        }
    }
}

@Composable
fun LemuroidSettingsSlider(
    index: Int = 0,
    modifier: Modifier = Modifier,
    state: SettingValueState<Int>,
    steps: Int,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit = { },
) {
    val itemCount = LocalSettingsItemCount.current
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index, itemCount),
        colors = lemuroidSegmentedSettingsColors(),
        modifier = modifier,
        enabled = enabled,
        supportingContent = {
            Column {
                subtitle()
                Slider(
                    value = state.value.toFloat(),
                    onValueChange = { state.value = it.roundToInt() },
                    steps = steps,
                    valueRange = valueRange,
                    enabled = enabled,
                )
            }
        },
        content = title,
    )
}

@Composable
private fun SettingsGroupTitleSmall(title: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val primary = MaterialTheme.colorScheme.primary
        val titleStyle = MaterialTheme.typography.labelLarge.copy(color = primary)
        ProvideTextStyle(value = titleStyle) { title() }
    }
}

@Composable
private fun lemuroidSegmentedSettingsColors(): ListItemColors {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    return ListItemDefaults.segmentedColors(
        containerColor = containerColor,
        disabledContainerColor = containerColor,
    )
}
