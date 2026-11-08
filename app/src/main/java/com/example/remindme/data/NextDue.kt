package com.example.remindme.data

import java.util.Calendar

data class Occurrence(val start: Long, val end: Long, val allDay: Boolean) {
    val sortKey: Long get() = if (allDay) end else start
}


fun TaskEntity.nextDue(now: Long = System.currentTimeMillis()): Occurrence? {
    val rule = repeatRule ?: when {
        untilDate != null || (dueDate == null && dueTimeMinutes != null) -> RepeatRule(unit = RepeatUnit.Day)
        else -> null
    }
    if (rule == null) return dueDate?.let(::occurrenceOn)

    val anchor = dueDate ?: localEpochDay(createdAt)
    val firstDay = maxOf(anchor, localEpochDay(now))

    if (rule.unit == RepeatUnit.Hour) return nextHourly(rule.every.coerceAtLeast(1), anchor, firstDay, now)

    val cycleDays = rule.every.coerceAtLeast(1) * when (rule.unit) {
        RepeatUnit.Day -> 1
        RepeatUnit.Week -> 7
        RepeatUnit.Month -> 31
        else -> 366
    }
    for (day in firstDay..firstDay + cycleDays + 1) {
        if (untilDate != null && day > untilDate) break
        if (!rule.fallsOn(day, anchor)) continue
        val occurrence = occurrenceOn(day)
        if (occurrence.end > now) return occurrence
    }
    return occurrenceOn(untilDate ?: anchor)
}

private fun TaskEntity.occurrenceOn(day: Long): Occurrence {
    val (y, m, d) = dateOfEpochDay(day)
    val time = dueTimeMinutes ?: return Occurrence(dateAt(y, m, d), dayStart(day + 1), allDay = true)
    val start = dateAt(y, m, d, time / 60, time % 60)
    val end = windowEndMinutes?.let { endMinutes ->
        // A window like 22:00–02:00 ends the next day
        val endDay = if (endMinutes <= time) day + 1 else day
        val (ey, em, ed) = dateOfEpochDay(endDay)
        dateAt(ey, em, ed, endMinutes / 60, endMinutes % 60)
    } ?: start
    return Occurrence(start, end, allDay = false)
}

/** [every] hours: inside the time window each day if there is one, otherwise around the clock. */
private fun TaskEntity.nextHourly(every: Int, anchor: Long, firstDay: Long, now: Long): Occurrence {
    val stepMs = every * 60 * 60_000L
    val windowStart = dueTimeMinutes ?: 0
    val windowEnd = windowEndMinutes

    if (windowEnd == null || windowEnd <= windowStart) {
        val (y, m, d) = dateOfEpochDay(anchor)
        val base = dateAt(y, m, d, windowStart / 60, windowStart % 60)
        val steps = if (now <= base) 0 else (now - base + stepMs - 1) / stepMs
        val next = base + steps * stepMs
        val last = untilDate?.let { dayStart(it + 1) }
        return if (last != null && next >= last) occurrenceOn(untilDate!!) else Occurrence(next, next, allDay = false)
    }

    // The window's start time always fits, so the next day at the latest has one
    for (day in firstDay..firstDay + 1) {
        if (untilDate != null && day > untilDate) break
        val (y, m, d) = dateOfEpochDay(day)
        var minutes = windowStart
        while (minutes <= windowEnd) {
            val at = dateAt(y, m, d, minutes / 60, minutes % 60)
            if (at >= now) return Occurrence(at, at, allDay = false)
            minutes += every * 60
        }
    }
    return occurrenceOn(untilDate ?: anchor)
}

private fun RepeatRule.fallsOn(day: Long, anchor: Long): Boolean {
    val n = every.coerceAtLeast(1)
    return when (unit) {
        RepeatUnit.Hour, RepeatUnit.Day -> (day - anchor) % n == 0L
        RepeatUnit.Week -> {
            val weekdays = days.ifEmpty { setOf(weekdayOf(anchor)) }
            weekdayOf(day) in weekdays && (weekIndex(day) - weekIndex(anchor)) % n == 0L
        }
        RepeatUnit.Month -> {
            val a = dateOfEpochDay(anchor)
            val date = dateOfEpochDay(day)
            val months = (date.year - a.year) * 12 + (date.month - a.month)
            months % n == 0 && date.day == minOf(a.day, daysInMonth(date.year, date.month))
        }
        RepeatUnit.Year -> {
            val a = dateOfEpochDay(anchor)
            val date = dateOfEpochDay(day)
            (date.year - a.year) % n == 0 && date.month == a.month &&
                date.day == minOf(a.day, daysInMonth(date.year, date.month))
        }
    }
}

private fun weekdayOf(day: Long): Int = Math.floorMod(day + 4, 7L).toInt() + 1
private fun weekIndex(day: Long): Long = Math.floorDiv(day + 3, 7L)

private fun daysInMonth(year: Int, month: Int): Int =
    Calendar.getInstance().apply { clear(); set(year, month, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)

private fun dayStart(day: Long): Long = dateOfEpochDay(day).let { (y, m, d) -> dateAt(y, m, d) }

private fun localEpochDay(millis: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return epochDayOf(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
}
