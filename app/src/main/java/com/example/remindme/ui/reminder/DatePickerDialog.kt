package com.example.remindme.ui.reminder

import com.example.remindme.ui.components.DialogConfirmButton
import com.example.remindme.ui.components.DialogDismissButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import com.example.remindme.ui.theme.RemindTheme
import java.util.Calendar
import java.util.TimeZone
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.RemindMeTheme

@Composable
internal fun DatePickerDialog(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    onConfirm: (year: Int, month: Int, day: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = RemindTheme.colors
    val today = Calendar.getInstance()
    val todayUtc = utcMidnight(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
    val state = rememberDatePickerState(
        initialSelectedDateMillis = utcMidnight(initialYear, initialMonth, initialDay).coerceAtLeast(todayUtc),
        yearRange = today.get(Calendar.YEAR)..(today.get(Calendar.YEAR) + 10),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayUtc
            override fun isSelectableYear(year: Int) = year >= today.get(Calendar.YEAR)
        }
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            DialogConfirmButton("OK") {
                state.selectedDateMillis?.let { millis ->
                    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
                    onConfirm(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
                } ?: onDismiss()
            }
        },
        dismissButton = { DialogDismissButton(onClick = onDismiss) },
        colors = DatePickerDefaults.colors(containerColor = colors.bgReminderCard)
    ) {
        DatePicker(
            state = state,
            colors = DatePickerDefaults.colors(
                containerColor = colors.bgReminderCard,
                selectedDayContainerColor = colors.chipSelected,
                selectedDayContentColor = colors.onChipSelected,
                selectedYearContainerColor = colors.chipSelected,
                selectedYearContentColor = colors.onChipSelected,
                todayDateBorderColor = colors.textBrand,
                todayContentColor = colors.textBrand
            )
        )
    }
}

private fun utcMidnight(year: Int, month: Int, day: Int): Long =
    Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(year, month, day)
    }.timeInMillis

@PreviewLightDark
@Composable
private fun DatePickerDialogPreview() {
    val today = Calendar.getInstance()
    RemindMeTheme {
        DatePickerDialog(
            initialYear = today.get(Calendar.YEAR),
            initialMonth = today.get(Calendar.MONTH),
            initialDay = today.get(Calendar.DAY_OF_MONTH),
            onConfirm = { _, _, _ -> },
            onDismiss = {}
        )
    }
}
