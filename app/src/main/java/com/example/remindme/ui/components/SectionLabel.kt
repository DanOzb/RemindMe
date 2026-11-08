package com.example.remindme.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = RemindTheme.colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

@PreviewLightDark
@Composable
private fun SectionLabelPreview() {
    PreviewSurface {
        SectionLabel("Your reminders")
        SectionLabel("Up next") {
            Text("See all", style = MaterialTheme.typography.labelMedium, color = RemindTheme.colors.textBrand)
        }
    }
}
