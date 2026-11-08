package com.example.remindme.ui.reminder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.components.RemindTextField
import com.example.remindme.ui.components.ScreenHeader
import com.example.remindme.data.place.SearchOutcome
import com.example.remindme.ui.components.TagPill
import com.example.remindme.ui.theme.RemindMeTheme
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing

@Composable
fun ReminderContent(
    form: ReminderForm,
    onFormChange: (ReminderForm) -> Unit,
    onSearchPlaces: suspend (String) -> SearchOutcome,
    isEdit: Boolean,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RemindTheme.colors
    var timePickerFor by rememberSaveable { mutableStateOf<RangeEnd?>(null) }
    var datePickerFor by rememberSaveable { mutableStateOf<RangeEnd?>(null) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    // Accordion: opening a card closes the others, so the screen stays short
    var openSection by rememberSaveable { mutableStateOf<ReminderSection?>(null) }
    val toggle = { section: ReminderSection ->
        openSection = if (openSection == section) null else section
    }

    Scaffold(
        modifier = modifier,
        containerColor = colors.bgPrimary,
        topBar = {
            ModalTopBar(
                title = if (isEdit) "Edit reminder" else "New reminder",
                onBack = onClose,
                onClose = onClose
            )
        },
        bottomBar = {
            PrimaryButton(
                text = if (isEdit) "Save" else "Create reminder",
                onClick = onSave,
                icon = Icons.Default.Check,
                enabled = form.isValid,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                    .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
        ) {
            ScreenHeader(if (isEdit) "Edit Reminder" else "Create Reminder")
            Spacer(Modifier.height(Spacing.lg))

            RemindText("Remind me to")
            Spacer(Modifier.height(Spacing.sm))
            RemindTextField(
                value = form.title,
                onValueChange = { onFormChange(form.copy(title = it)) },
                placeholder = "Buy apples",
                leadingIcon = Icons.Outlined.Edit
            )
            Spacer(Modifier.height(Spacing.xl))

            // Reserve the pill's touch-target height, so the row doesn't jump when it appears
            Row(Modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                RemindText("Remind me when…")
                if (form.hasBoth) {
                    Spacer(Modifier.width(Spacing.sm))
                    TagPill(
                        text = form.match.label,
                        onClickLabel = "Switch between both and either",
                        onClick = {
                            val next = if (form.match == MatchRule.Both) MatchRule.Either else MatchRule.Both
                            onFormChange(form.copy(match = next))
                        }
                    )
                }
            }
            Spacer(Modifier.height(Spacing.md))

            LocationCard(
                form = form,
                onFormChange = onFormChange,
                onSearchPlaces = onSearchPlaces,
                expanded = openSection == ReminderSection.Location,
                onToggle = { toggle(ReminderSection.Location) }
            )
            Spacer(Modifier.height(Spacing.sm))

            TimeCard(
                form = form,
                onTimeModeChange = { mode ->
                    onFormChange(form.withTimeMode(mode))
                    if (mode == TimeMode.At) timePickerFor = RangeEnd.Start
                },
                onPickTime = { timePickerFor = it },
                expanded = openSection == ReminderSection.Time,
                onToggle = { toggle(ReminderSection.Time) }
            )
            Spacer(Modifier.height(Spacing.sm))

            DateCard(
                form = form,
                onDateModeChange = { mode ->
                    onFormChange(form.withDateMode(mode))
                    if (mode == DateMode.On) datePickerFor = RangeEnd.Start
                },
                onPickDate = { datePickerFor = it },
                expanded = openSection == ReminderSection.Date,
                onToggle = { toggle(ReminderSection.Date) }
            )
            Spacer(Modifier.height(Spacing.sm))

            RepeatCard(
                form = form,
                onFormChange = onFormChange,
                expanded = openSection == ReminderSection.Repeat,
                onToggle = { toggle(ReminderSection.Repeat) }
            )
            Spacer(Modifier.height(Spacing.md))

            PlainWordsCard(form)

            if (onDelete != null) {
                Spacer(Modifier.height(Spacing.lg))
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.textSecondary)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delete reminder", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(Spacing.lg))
        }
    }

    timePickerFor?.let { target ->
        val isEnd = target == RangeEnd.End
        TimePickerDialog(
            initialHour = if (isEnd) form.endHour else form.hour,
            initialMinute = if (isEnd) form.endMinute else form.minute,
            onConfirm = { hour, minute ->
                onFormChange(if (isEnd) form.withEndTime(hour, minute) else form.withStartTime(hour, minute))
                timePickerFor = null
            },
            onDismiss = { timePickerFor = null }
        )
    }

    datePickerFor?.let { target ->
        val isEnd = target == RangeEnd.End
        DatePickerDialog(
            initialYear = if (isEnd) form.endYear else form.year,
            initialMonth = if (isEnd) form.endMonth else form.month,
            initialDay = if (isEnd) form.endDay else form.day,
            onConfirm = { year, month, day ->
                onFormChange(if (isEnd) form.withEndDate(year, month, day) else form.withStartDate(year, month, day))
                datePickerFor = null
            },
            onDismiss = { datePickerFor = null }
        )
    }

    if (showDeleteConfirm && onDelete != null) {
        DeleteReminderDialog(
            title = form.title.trim(),
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@PreviewLightDark
@Composable
private fun CreateReminderPreview() {
    RemindMeTheme {
        ReminderContent(
            form = ReminderForm(),
            onFormChange = {},
            onSearchPlaces = { SearchOutcome.Found(emptyList()) },
            isEdit = false,
            onSave = {},
            onDelete = null,
            onClose = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun EditReminderPreview() {
    RemindMeTheme {
        ReminderContent(
            form = ReminderForm(
                title = "Take out the trash",
                place = "Home",
                address = "Rädisvägen 12, 123 45 Stockholm, Sweden",
                latitude = 59.33,
                longitude = 18.06,
                placeMode = PlaceMode.AwayFrom,
                timeMode = TimeMode.Between,
                hour = 9,
                endHour = 17,
                repeat = RepeatMode.EveryTime
            ),
            onFormChange = {},
            onSearchPlaces = { SearchOutcome.Found(emptyList()) },
            isEdit = true,
            onSave = {},
            onDelete = {},
            onClose = {}
        )
    }
}
