package com.example.remindme.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Shield

internal val SettingsRowIconSize = 20.dp

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = RemindTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textBrand,
            modifier = Modifier.size(SettingsRowIconSize)
        )
        Spacer(Modifier.width(Spacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.text
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
                Spacer(Modifier.width(Spacing.xs))
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(SettingsRowIconSize)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SettingsRowPreview() {
    PreviewSurface {
        RemindCard {
            SettingsRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                subtitle = "Gentle nudges, right on time",
                trailing = { RemindSwitch(checked = true, onCheckedChange = {}) }
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.Palette,
                title = "Appearance",
                subtitle = "Follow your device's theme",
                value = "System",
                onClick = {}
            )
            SettingsDivider()
            SettingsRow(icon = Icons.Outlined.Shield, title = "Privacy & data")
        }
    }
}
