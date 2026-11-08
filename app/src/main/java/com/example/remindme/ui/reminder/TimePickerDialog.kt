package com.example.remindme.ui.reminder

import com.example.remindme.ui.components.DialogConfirmButton
import com.example.remindme.ui.components.DialogDismissButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.RemindMeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = RemindTheme.colors
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { DialogConfirmButton("OK", onClick = { onConfirm(state.hour, state.minute) }) },
        dismissButton = { DialogDismissButton(onClick = onDismiss) },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = colors.bgSecondaryCards,
                    selectorColor = colors.textBrand,
                    timeSelectorSelectedContainerColor = colors.chipSelected,
                    timeSelectorSelectedContentColor = colors.onChipSelected
                )
            )
        },
        containerColor = colors.bgReminderCard
    )
}
