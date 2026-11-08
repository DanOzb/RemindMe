package com.example.remindme.ui.reminder

import com.example.remindme.ui.components.DialogConfirmButton
import com.example.remindme.ui.components.DialogDismissButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.RemindMeTheme

@Composable
internal fun DeleteReminderDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = RemindTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete reminder?") },
        text = { Text("\"$title\" will be removed.") },
        confirmButton = { DialogConfirmButton("Delete", onClick = onConfirm) },
        dismissButton = { DialogDismissButton(onClick = onDismiss) },
        containerColor = colors.bgReminderCard,
        titleContentColor = colors.text,
        textContentColor = colors.textSecondary
    )
}
