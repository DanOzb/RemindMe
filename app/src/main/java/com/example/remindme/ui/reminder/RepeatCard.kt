package com.example.remindme.ui.reminder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.components.ExpandableCard
import com.example.remindme.ui.theme.RemindShapes
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

@Composable
internal fun RepeatCard(
    form: ReminderForm,
    onFormChange: (ReminderForm) -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    var showCustom by rememberSaveable { mutableStateOf(false) }
    val selected = form.effectiveRepeat
    val options = RepeatMode.entries.filter { it != RepeatMode.EveryTime || form.hasPlace }

    ExpandableCard(
        icon = Icons.Outlined.Repeat,
        label = "Repeat",
        summary = repeatSummary(form),
        active = selected != RepeatMode.Once,
        expanded = expanded,
        onToggle = onToggle
    ) {
        Column(Modifier.selectableGroup()) {
            options.forEach { mode ->
                RepeatOptionRow(
                    label = optionLabel(mode, form),
                    selected = mode == selected,
                    onClick = {
                        if (mode == RepeatMode.Custom) showCustom = true
                        else onFormChange(form.withRepeat(mode))
                    }
                )
            }
        }
    }

    if (showCustom) {
        CustomRepeatDialog(
            // Start from what's showing, so "Weekdays" → Custom opens with Mon–Fri ticked
            initial = form.repeatRule ?: form.customRepeat,
            form = form,
            onConfirm = { rule ->
                onFormChange(form.withRepeat(RepeatMode.Custom, rule))
                showCustom = false
            },
            onDismiss = { showCustom = false }
        )
    }
}

@Composable
private fun RepeatOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = RemindTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(RemindShapes.field)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = colors.textBrand, unselectedColor = colors.textSecondary)
        )
        Spacer(Modifier.width(Spacing.md))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun optionLabel(mode: RepeatMode, form: ReminderForm): String = when (mode) {
    RepeatMode.Once -> "Don't repeat"
    RepeatMode.EveryTime -> if (form.placeMode == PlaceMode.AwayFrom) "Every time I leave" else "Every time I arrive"
    RepeatMode.Daily -> "Every day"
    RepeatMode.Weekdays -> "Every weekday (Mon–Fri)"
    RepeatMode.Weekly -> "Every week on ${weekdayName(form.startWeekday, short = false)}"
    RepeatMode.Custom ->
        if (form.effectiveRepeat == RepeatMode.Custom) "Custom: ${form.repeatRule?.let { describeRepeat(it, form) }}"
        else "Custom…"
}

private fun repeatSummary(form: ReminderForm): String = when (form.effectiveRepeat) {
    RepeatMode.Once -> "Once"
    RepeatMode.EveryTime -> "Every time"
    else -> form.repeatRule?.let { describeRepeat(it, form) }?.replaceFirstChar { it.uppercase() } ?: "Once"
}

@PreviewLightDark
@Composable
private fun RepeatCardPreview() {
    PreviewSurface {
        RepeatCard(
            form = ReminderForm(place = "Home", repeat = RepeatMode.EveryTime),
            onFormChange = {},
            expanded = true,
            onToggle = {}
        )
        RepeatCard(
            form = ReminderForm(repeat = RepeatMode.Weekdays, timeMode = TimeMode.At),
            onFormChange = {},
            expanded = false,
            onToggle = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun RepeatOptionRowPreview() {
    PreviewSurface {
        RepeatOptionRow(label = "Every day", selected = true, onClick = {})
        RepeatOptionRow(label = "Don't repeat", selected = false, onClick = {})
    }
}
