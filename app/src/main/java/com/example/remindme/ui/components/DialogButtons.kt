package com.example.remindme.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface
import androidx.compose.foundation.layout.Row

@Composable
fun DialogConfirmButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled) {
        Text(text, color = RemindTheme.colors.textBrand.copy(alpha = if (enabled) 1f else 0.4f))
    }
}

@Composable
fun DialogDismissButton(onClick: () -> Unit, text: String = "Cancel") {
    TextButton(onClick = onClick) {
        Text(text, color = RemindTheme.colors.textSecondary)
    }
}

@PreviewLightDark
@Composable
private fun DialogButtonsPreview() {
    PreviewSurface {
        Row {
            DialogDismissButton(onClick = {})
            DialogConfirmButton("OK", onClick = {})
            DialogConfirmButton("Done", enabled = false, onClick = {})
        }
    }
}
