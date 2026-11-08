package com.example.remindme.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.PreviewSurface
import com.example.remindme.ui.theme.RemindTheme

/** [onTestNotification] adds a debug-only item; null hides it. */
@Composable
internal fun HomeMenuButton(onOpenSettings: () -> Unit, onTestNotification: (() -> Unit)? = null) {
    val colors = RemindTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                modifier = Modifier.size(24.dp),
                tint = colors.text
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = colors.bgReminderCard
        ) {
            DropdownMenuItem(
                text = { Text("Settings", style = MaterialTheme.typography.bodyLarge, color = colors.text) },
                onClick = {
                    expanded = false
                    onOpenSettings()
                }
            )
            if (onTestNotification != null) {
                DropdownMenuItem(
                    text = { Text("Test notification", style = MaterialTheme.typography.bodyLarge, color = colors.text) },
                    onClick = {
                        expanded = false
                        onTestNotification()
                    }
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeMenuButtonPreview() {
    PreviewSurface {
        HomeMenuButton(onOpenSettings = {}, onTestNotification = {})
    }
}
