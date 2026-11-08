package com.example.remindme.ui.reminder

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.remindme.ui.components.ChoiceChipRow
import com.example.remindme.ui.components.ExpandableCard
import com.example.remindme.ui.format.formatDate
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

@Composable
internal fun DateCard(
    form: ReminderForm,
    onDateModeChange: (DateMode) -> Unit,
    onPickDate: (RangeEnd) -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    ExpandableCard(
        icon = Icons.Outlined.CalendarToday,
        label = "Date",
        summary = dateSummary(form),
        active = form.hasDate,
        expanded = expanded,
        onToggle = onToggle
    ) {
        ChoiceChipRow(
            options = DateMode.entries,
            selected = form.dateMode,
            onSelect = onDateModeChange,
            label = { it.label }
        )
        Spacer(Modifier.height(Spacing.md))
        when (form.dateMode) {
            DateMode.AnyDay -> CardHint(
                if (form.hasTime) "The next time it's ${form.timeLabel}" else "No particular day"
            )
            DateMode.On -> PickerRow(value = form.startDateLabel, onClick = { onPickDate(RangeEnd.Start) })
            DateMode.Between -> {
                PickerRow(label = "From", value = form.startDateLabel, onClick = { onPickDate(RangeEnd.Start) })
                PickerRow(label = "To", value = form.endDateLabel, onClick = { onPickDate(RangeEnd.End) })
            }
        }
    }
}

internal val ReminderForm.startDateLabel: String get() = formatDate(year, month, day)
internal val ReminderForm.endDateLabel: String get() = formatDate(endYear, endMonth, endDay)

private fun dateSummary(form: ReminderForm): String = when (form.dateMode) {
    DateMode.AnyDay -> "Any day"
    DateMode.On -> form.startDateLabel
    DateMode.Between -> "${form.startDateLabel} – ${form.endDateLabel}"
}

@PreviewLightDark
@Composable
private fun DateCardPreview() {
    PreviewSurface {
        DateCard(
            form = ReminderForm().withDateMode(DateMode.Between),
            onDateModeChange = {},
            onPickDate = {},
            expanded = true,
            onToggle = {}
        )
        DateCard(
            form = ReminderForm(),
            onDateModeChange = {},
            onPickDate = {},
            expanded = false,
            onToggle = {}
        )
    }
}
