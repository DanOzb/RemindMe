package com.example.remindme.ui.reminder

import com.example.remindme.data.RepeatRule
import com.example.remindme.data.RepeatUnit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal fun describeRepeat(rule: RepeatRule, form: ReminderForm): String {
    val n = rule.every
    return when (rule.unit) {
        RepeatUnit.Hour -> if (n == 1) "every hour" else "every $n hours"
        RepeatUnit.Day -> if (n == 1) "every day" else "every $n days"
        RepeatUnit.Week -> {
            val days = weekOrder().filter { it in rule.days }
            when {
                n == 1 && rule.days == RepeatRule.Weekdays -> "every weekday"
                n == 1 && days.size == 7 -> "every day"
                n == 1 && days.size == 1 -> "every ${weekdayName(days.single(), short = false)}"
                n == 1 -> "every ${joinNames(days.map { weekdayName(it, short = true) })}"
                else -> "every $n weeks on ${joinNames(days.map { weekdayName(it, short = true) })}"
            }
        }
        RepeatUnit.Month -> {
            val on = "on the ${ordinal(form.day)}"
            if (n == 1) "every month $on" else "every $n months $on"
        }
        RepeatUnit.Year -> {
            val on = "on ${monthDay(form.month, form.day)}"
            if (n == 1) "every year $on" else "every $n years $on"
        }
    }
}

internal fun RepeatUnit.label(count: Int): String {
    val word = name.lowercase()
    return if (count == 1) word else "${word}s"
}

internal fun weekOrder(): List<Int> {
    val first = Calendar.getInstance().firstDayOfWeek
    return (0 until 7).map { (first - 1 + it) % 7 + 1 }
}

internal fun weekdayName(day: Int, short: Boolean): String =
    SimpleDateFormat(if (short) "EEE" else "EEEE", Locale.getDefault()).format(weekdayDate(day))

internal fun weekdayInitial(day: Int): String =
    SimpleDateFormat("EEEEE", Locale.getDefault()).format(weekdayDate(day))

private fun weekdayDate(day: Int) = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, day) }.time

private fun monthDay(month: Int, day: Int): String {
    val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMd")
    val date = Calendar.getInstance().apply { clear(); set(2000, month, day) }.time
    return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}

private fun joinNames(names: List<String>): String = when (names.size) {
    0 -> ""
    1 -> names.single()
    else -> names.dropLast(1).joinToString(", ") + " and " + names.last()
}

private fun ordinal(n: Int): String {
    val suffix = if (n % 100 in 11..13) "th" else when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
    return "$n$suffix"
}
