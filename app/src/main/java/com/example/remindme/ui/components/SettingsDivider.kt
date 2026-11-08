package com.example.remindme.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Shield

@Composable
fun SettingsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = Spacing.lg + SettingsRowIconSize + Spacing.md),
        thickness = 1.dp,
        color = RemindTheme.colors.border
    )
}

@PreviewLightDark
@Composable
private fun SettingsDividerPreview() {
    PreviewSurface {
        RemindCard {
            SettingsRow(icon = Icons.Outlined.Palette, title = "Appearance")
            SettingsDivider()
            SettingsRow(icon = Icons.Outlined.Shield, title = "Privacy & data")
        }
    }
}
