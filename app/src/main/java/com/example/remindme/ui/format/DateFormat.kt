package com.example.remindme.ui.format

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun formatDate(year: Int, month: Int, day: Int): String {
    val date = Calendar.getInstance().apply { clear(); set(year, month, day) }
    val today = Calendar.getInstance()
    return when {
        sameDay(date, today) -> "Today"
        sameDay(date, today.apply { add(Calendar.DAY_OF_YEAR, 1) }) -> "Tomorrow"
        else -> {
            val skeleton = if (year == Calendar.getInstance().get(Calendar.YEAR)) "EEEMMMd" else "EEEMMMdyyyy"
            val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)
            SimpleDateFormat(pattern, Locale.getDefault()).format(date.time)
        }
    }
}

fun formatDate(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return formatDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
}

fun formatDue(millis: Long, allDay: Boolean): String {
    val date = formatDate(millis)
    return when {
        allDay -> date
        date == "Today" -> formatTime(millis)
        else -> "$date, ${formatTime(millis)}"
    }
}

private fun sameDay(a: Calendar, b: Calendar) =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
