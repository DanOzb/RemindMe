package com.example.remindme.ui.reminder

import com.example.remindme.data.RepeatRule
import com.example.remindme.data.RepeatUnit
import com.example.remindme.data.TaskEntity
import com.example.remindme.data.dateOfEpochDay
import com.example.remindme.data.epochDayOf

internal fun TaskEntity.toForm(): ReminderForm {
    val start = dueDate?.let(::dateOfEpochDay)
    val until = untilDate?.let(::dateOfEpochDay)

    var form = ReminderForm(
        title = title,
        // A place saved without coordinates (from before search existed) shows up as a search query
        place = placeName.orEmpty(),
        address = address.orEmpty(),
        latitude = latitude,
        longitude = longitude,
        placeMode = if (awayFromPlace) PlaceMode.AwayFrom else PlaceMode.Near,
        radiusMeters = radiusMeters ?: 200,
        timeMode = when {
            dueTimeMinutes == null -> TimeMode.AnyTime
            windowEndMinutes != null -> TimeMode.Between
            else -> TimeMode.At
        },
        hour = dueTimeMinutes?.let { it / 60 } ?: DefaultReminderHour,
        minute = dueTimeMinutes?.let { it % 60 } ?: DefaultReminderMinute,
        dateMode = when {
            start == null -> DateMode.AnyDay
            until != null -> DateMode.Between
            else -> DateMode.On
        },
        match = if (matchEither) MatchRule.Either else MatchRule.Both,
    )
    windowEndMinutes?.let { form = form.copy(endHour = it / 60, endMinute = it % 60) }
    if (start != null) {
        val end = until ?: start
        form = form.copy(
            year = start.year, month = start.month, day = start.day,
            endYear = end.year, endMonth = end.month, endDay = end.day,
        )
    }
    return form.withSavedRepeat(repeats, repeatRule)
}

private fun ReminderForm.withSavedRepeat(everyTime: Boolean, rule: RepeatRule?): ReminderForm = when {
    everyTime -> copy(repeat = RepeatMode.EveryTime)
    rule == null -> copy(repeat = RepeatMode.Once)
    rule == RepeatRule(unit = RepeatUnit.Day) -> copy(repeat = RepeatMode.Daily)
    rule == RepeatRule(unit = RepeatUnit.Week, days = RepeatRule.Weekdays) -> copy(repeat = RepeatMode.Weekdays)
    rule == RepeatRule(unit = RepeatUnit.Week, days = setOf(startWeekday)) -> copy(repeat = RepeatMode.Weekly)
    else -> copy(repeat = RepeatMode.Custom, customRepeat = rule)
}

/** Saving re-arms the reminder: it's marked active and can fire again. */
internal fun ReminderForm.toTask(existing: TaskEntity?): TaskEntity {
    val base = existing ?: TaskEntity(title = "")
    return base.copy(
        title = title.trim(),
        completed = false,
        completedAt = null,
        placeName = if (hasPlace) placeLabel else null,
        address = if (hasPlace) address.ifBlank { null } else null,
        latitude = if (hasPlace) latitude else null,
        longitude = if (hasPlace) longitude else null,
        radiusMeters = if (hasPlace) radiusMeters else null,
        awayFromPlace = hasPlace && placeMode == PlaceMode.AwayFrom,
        dueDate = if (hasDate) epochDayOf(year, month, day) else null,
        dueTimeMinutes = if (hasTime) hour * 60 + minute else null,
        windowEndMinutes = if (timeMode == TimeMode.Between) endHour * 60 + endMinute else null,
        untilDate = if (dateMode == DateMode.Between) epochDayOf(endYear, endMonth, endDay) else null,
        matchEither = hasBoth && match == MatchRule.Either,
        repeats = repeatsEveryTime,
        repeatRule = repeatRule,
        lastFiredAt = null,
    )
}
