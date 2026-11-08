package com.example.remindme.ui.components

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface
import androidx.compose.foundation.layout.Row

@Composable
fun RemindSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = RemindTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedTrackColor = colors.textBrand,
            checkedThumbColor = colors.textOnBrand,
            checkedBorderColor = colors.textBrand,
            uncheckedTrackColor = colors.bgSecondaryCards,
            uncheckedThumbColor = colors.textSecondary,
            uncheckedBorderColor = colors.border
        )
    )
}

@PreviewLightDark
@Composable
private fun RemindSwitchPreview() {
    PreviewSurface {
        Row {
            RemindSwitch(checked = true, onCheckedChange = {})
            RemindSwitch(checked = false, onCheckedChange = {})
        }
    }
}
