package com.example.remindme.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

@Composable
fun ScreenHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium,
        color = RemindTheme.colors.text
    )
}

@PreviewLightDark
@Composable
private fun ScreenHeaderPreview() {
    PreviewSurface {
        ScreenHeader("Create Reminder")
    }
}
