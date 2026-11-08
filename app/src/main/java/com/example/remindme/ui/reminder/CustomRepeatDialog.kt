package com.example.remindme.ui.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.remindme.data.RepeatRule
import com.example.remindme.data.RepeatUnit
import com.example.remindme.ui.components.DialogConfirmButton
import com.example.remindme.ui.components.DialogDismissButton
import com.example.remindme.ui.theme.RemindShapes
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.RemindMeTheme
import com.example.remindme.ui.theme.PreviewSurface
import java.util.Calendar

private const val MaxEvery = 99

@Composable
internal fun CustomRepeatDialog(
    initial: RepeatRule,
    form: ReminderForm,
    onConfirm: (RepeatRule) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = RemindTheme.colors
    var every by rememberSaveable { mutableStateOf(initial.every) }
    var unit by rememberSaveable { mutableStateOf(initial.unit) }
    var days by rememberSaveable {
        mutableStateOf(initial.days.ifEmpty { setOf(form.startWeekday) }.toList())
    }
    val rule = RepeatRule(every, unit, if (unit == RepeatUnit.Week) days.toSet() else emptySet())
    val canSave = unit != RepeatUnit.Week || days.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom repeat") },
        text = {
            Column {
                Text("Repeat every", style = MaterialTheme.typography.bodyMedium, color = colors.text)
                Spacer(Modifier.height(Spacing.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EveryStepper(value = every, onValueChange = { every = it })
                    Spacer(Modifier.width(Spacing.sm))
                    UnitDropdown(
                        unit = unit,
                        count = every,
                        onSelect = { unit = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (unit == RepeatUnit.Week) {
                    Spacer(Modifier.height(Spacing.lg))
                    Text("On", style = MaterialTheme.typography.bodyMedium, color = colors.text)
                    Spacer(Modifier.height(Spacing.sm))
                    WeekdayToggles(
                        selected = days.toSet(),
                        onToggle = { day -> days = if (day in days) days - day else days + day }
                    )
                }
                Spacer(Modifier.height(Spacing.lg))
                CardHint(
                    if (canSave) describeRepeat(rule, form).replaceFirstChar { it.uppercase() }
                    else "Pick at least one day"
                )
            }
        },
        confirmButton = { DialogConfirmButton("Done", enabled = canSave) { onConfirm(rule) } },
        dismissButton = { DialogDismissButton(onClick = onDismiss) },
        containerColor = colors.bgReminderCard,
        titleContentColor = colors.text,
        textContentColor = colors.text
    )
}

@Composable
private fun EveryStepper(value: Int, onValueChange: (Int) -> Unit) {
    val colors = RemindTheme.colors

    Row(
        modifier = Modifier
            .clip(RemindShapes.field)
            .background(colors.bgSecondaryCards),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onValueChange(value - 1) }, enabled = value > 1) {
            Icon(Icons.Filled.Remove, contentDescription = "Fewer", tint = colors.text.copy(alpha = if (value > 1) 1f else 0.3f))
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = colors.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 24.dp)
        )
        IconButton(onClick = { onValueChange(value + 1) }, enabled = value < MaxEvery) {
            Icon(Icons.Filled.Add, contentDescription = "More", tint = colors.text)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    unit: RepeatUnit,
    count: Int,
    onSelect: (RepeatUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RemindTheme.colors
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = unit.label(count),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            shape = RemindShapes.field,
            textStyle = MaterialTheme.typography.bodyLarge,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.textBrand,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.text,
                unfocusedTextColor = colors.text
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = colors.bgReminderCard
        ) {
            RepeatUnit.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label(count), color = colors.text) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun WeekdayToggles(selected: Set<Int>, onToggle: (Int) -> Unit) {
    val colors = RemindTheme.colors

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        weekOrder().forEach { day ->
            val on = day in selected
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    // Filled brand color: a pale chip is too easy to miss at this size
                    .background(if (on) colors.textBrand else colors.bgSecondaryCards)
                    .toggleable(value = on, role = Role.Checkbox, onValueChange = { onToggle(day) })
                    .semantics { contentDescription = weekdayName(day, short = false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = weekdayInitial(day),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (on) colors.textOnBrand else colors.textSecondary
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CustomRepeatDialogPreview() {
    RemindMeTheme {
        CustomRepeatDialog(
            initial = RepeatRule(every = 2, unit = RepeatUnit.Week, days = setOf(Calendar.MONDAY, Calendar.THURSDAY)),
            form = ReminderForm(),
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun EveryStepperPreview() {
    PreviewSurface {
        EveryStepper(value = 1, onValueChange = {})
        EveryStepper(value = 12, onValueChange = {})
    }
}

@PreviewLightDark
@Composable
private fun UnitDropdownPreview() {
    PreviewSurface {
        UnitDropdown(unit = RepeatUnit.Week, count = 2, onSelect = {})
    }
}

@PreviewLightDark
@Composable
private fun WeekdayTogglesPreview() {
    PreviewSurface {
        WeekdayToggles(selected = RepeatRule.Weekdays, onToggle = {})
    }
}
