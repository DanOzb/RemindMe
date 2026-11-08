package com.example.remindme.ui.reminder

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.example.remindme.data.RepeatUnit

internal fun plainWords(form: ReminderForm): AnnotatedString {
    val rule = form.repeatRule
    if (!form.hasPlace && !form.hasWhen && rule == null) {
        return AnnotatedString("Add a place, a time, a date or a repeat so I know when to remind you.")
    }

    val schedule = rule?.let { listOf(Segment(describeRepeat(it, form), bold = true)) }.orEmpty()
    val where = if (form.hasPlace) {
        val lead = if (form.repeatsEveryTime) "every time I'm " else "when I'm "
        val relation = if (form.placeMode == PlaceMode.Near) "near " else "away from "
        listOf(Segment(lead + relation), Segment(form.placeLabel, bold = true))
    } else emptyList()
    val whenParts = if (form.hasWhen) whenSegments(form) else emptyList()

    val segments = buildList {
        when {
            !form.hasPlace -> {
                addAll(schedule)
                if (schedule.isNotEmpty() && whenParts.isNotEmpty()) add(Segment(" "))
                addAll(whenParts)
            }
            form.hasWhen && form.match == MatchRule.Either -> {
                addAll(where)
                add(Segment(", or "))
                addAll(schedule)
                if (schedule.isNotEmpty()) add(Segment(" "))
                addAll(whenParts)
            }
            else -> {
                if (schedule.isNotEmpty()) { addAll(schedule); add(Segment(", ")) }
                addAll(where)
                if (whenParts.isNotEmpty()) { add(Segment(" ")); addAll(whenParts) }
            }
        }
        add(Segment(", remind me to "))
        val task = form.title.trim().trimEnd('.').ifBlank { "…" }
        add(Segment(task.replaceFirstChar { it.lowercase() }, bold = true))
        add(Segment("."))
    }

    return buildAnnotatedString {
        val bold = SpanStyle(fontWeight = FontWeight.SemiBold)
        segments.forEachIndexed { i, segment ->
            val text = if (i == 0) segment.text.replaceFirstChar { it.uppercase() } else segment.text
            if (segment.bold) withStyle(bold) { append(text) } else append(text)
        }
    }
}

private data class Segment(val text: String, val bold: Boolean = false)

private fun whenSegments(form: ReminderForm): List<Segment> = buildList {
    val rule = form.repeatRule
    when (form.dateMode) {
        DateMode.AnyDay -> Unit
        DateMode.On -> {
            val date = form.startDateLabel.lowercaseRelative()
            // A repeating reminder's single date is where it starts, not the only day it fires
            val lead = when {
                rule != null -> "starting "
                date == "today" || date == "tomorrow" -> ""
                else -> "on "
            }
            if (lead.isNotEmpty()) add(Segment(lead))
            add(Segment(date, bold = true))
        }
        DateMode.Between -> {
            add(Segment("from ")); add(Segment(form.startDateLabel.lowercaseRelative(), bold = true))
            add(Segment(" to ")); add(Segment(form.endDateLabel.lowercaseRelative(), bold = true))
        }
    }
    if (form.hasDate && form.hasTime) add(Segment(" "))
    when (form.timeMode) {
        TimeMode.AnyTime -> Unit
        TimeMode.At -> {
            add(Segment(if (rule?.unit == RepeatUnit.Hour) "from " else "at "))
            add(Segment(form.timeLabel, bold = true))
        }
        TimeMode.Between -> {
            add(Segment("between ")); add(Segment(form.timeLabel, bold = true))
            add(Segment(" and ")); add(Segment(form.endTimeLabel, bold = true))
        }
    }
}

private fun String.lowercaseRelative(): String =
    if (this == "Today" || this == "Tomorrow") lowercase() else this
