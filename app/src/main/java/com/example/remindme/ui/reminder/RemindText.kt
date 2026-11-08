package com.example.remindme.ui.reminder

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

@Composable
fun RemindText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = RemindTheme.colors.text
    )
}


@PreviewLightDark
@Composable
private fun RemindTextPreview() {
    PreviewSurface {
        RemindText("Remind me to")
    }
}
