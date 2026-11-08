package com.example.remindme.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.components.DialogDismissButton
import com.example.remindme.ui.theme.Appearance
import com.example.remindme.ui.theme.RemindMeTheme
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing

@Composable
internal fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    description: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = RemindTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = option == selected,
                                role = Role.RadioButton,
                                onClick = { onSelect(option) }
                            )
                            .padding(vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = null, // the whole row is the touch target
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.textBrand,
                                unselectedColor = colors.textSecondary
                            )
                        )
                        Spacer(Modifier.width(Spacing.md))
                        Column {
                            Text(label(option), style = MaterialTheme.typography.bodyLarge, color = colors.text)
                            Text(description(option), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { DialogDismissButton(onClick = onDismiss) },
        containerColor = colors.bgReminderCard,
        titleContentColor = colors.text,
        textContentColor = colors.textSecondary
    )
}

@PreviewLightDark
@Composable
private fun ChoiceDialogPreview() {
    RemindMeTheme {
        ChoiceDialog(
            title = "Appearance",
            options = Appearance.entries,
            selected = Appearance.System,
            label = { it.label },
            description = { it.description },
            onSelect = {},
            onDismiss = {}
        )
    }
}
