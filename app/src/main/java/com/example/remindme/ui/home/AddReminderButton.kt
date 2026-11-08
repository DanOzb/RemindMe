package com.example.remindme.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.remindme.ui.theme.RemindShapes
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.PreviewSurface

@Composable
internal fun AddReminderButton(onClick: () -> Unit) {
    val colors = RemindTheme.colors
    IconButton(
        onClick = onClick,
        shape = RemindShapes.tile,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = colors.text
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "New reminder",
            modifier = Modifier.size(32.dp))
    }
}

@PreviewLightDark
@Composable
private fun AddReminderButtonPreview() {
    PreviewSurface {
        AddReminderButton(onClick = {})
    }
}
