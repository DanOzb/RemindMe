package com.example.remindme.ui.reminder

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.remindme.ui.components.ChoiceChipRow
import com.example.remindme.ui.components.ExpandableCard
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

internal enum class RangeEnd { Start, End }

@Composable
internal fun TimeCard(
    form: ReminderForm,
    onTimeModeChange: (TimeMode) -> Unit,
    onPickTime: (RangeEnd) -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    ExpandableCard(
        icon = Icons.Outlined.Schedule,
        label = "Time",
        summary = timeSummary(form),
        active = form.hasTime,
        expanded = expanded,
        onToggle = onToggle
    ) {
        ChoiceChipRow(
            options = form.timeOptions,
            selected = form.timeMode,
            onSelect = onTimeModeChange,
            label = { it.label }
        )
        Spacer(Modifier.height(Spacing.md))
        when (form.timeMode) {
            TimeMode.AnyTime -> CardHint(
                if (form.hasPlace) "Whenever the location matches, day or night"
                else "Add a place, or set a time"
            )
            TimeMode.At -> {
                PickerRow(value = form.timeLabel, onClick = { onPickTime(RangeEnd.Start) })
                if (form.timeAutoFilled) CardHint("This reminder needs a time to fire. Change it if ${form.timeLabel} doesn't suit.")
            }
            TimeMode.Between -> {
                PickerRow(label = "From", value = form.timeLabel, onClick = { onPickTime(RangeEnd.Start) })
                PickerRow(label = "To", value = form.endTimeLabel, onClick = { onPickTime(RangeEnd.End) })
                val overnight = form.endHour * 60 + form.endMinute < form.hour * 60 + form.minute
                when {
                    form.hour == form.endHour && form.minute == form.endMinute ->
                        CardHint("Pick an end time that's different from the start")
                    overnight -> CardHint("Runs overnight, ending the next day")
                }
            }
        }
    }
}

private fun timeSummary(form: ReminderForm): String = when (form.timeMode) {
    TimeMode.AnyTime -> "Any time"
    TimeMode.At -> "At ${form.timeLabel}"
    TimeMode.Between -> "${form.timeLabel}–${form.endTimeLabel}"
}

@PreviewLightDark
@Composable
private fun TimeCardPreview() {
    PreviewSurface {
        TimeCard(
            form = ReminderForm(timeMode = TimeMode.Between, hour = 22, endHour = 6),
            onTimeModeChange = {},
            onPickTime = {},
            expanded = true,
            onToggle = {}
        )
        TimeCard(
            form = ReminderForm(timeMode = TimeMode.At, hour = 18),
            onTimeModeChange = {},
            onPickTime = {},
            expanded = false,
            onToggle = {}
        )
    }
}
