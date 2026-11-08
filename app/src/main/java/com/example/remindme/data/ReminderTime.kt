package com.example.remindme.data

import java.util.Calendar
import java.util.TimeZone

private const val DAY_MS = 24 * 60 * 60 * 1000L
private val UTC = TimeZone.getTimeZone("UTC")

fun epochDayOf(year: Int, month: Int, day: Int): Long =
    Calendar.getInstance(UTC).apply { clear(); set(year, month, day) }.timeInMillis / DAY_MS

fun todayEpochDay(): Long {
    val now = Calendar.getInstance()
    return epochDayOf(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
}

data class YearMonthDay(val year: Int, val month: Int, val day: Int)

fun dateOfEpochDay(epochDay: Long): YearMonthDay {
    val cal = Calendar.getInstance(UTC).apply { timeInMillis = epochDay * DAY_MS }
    return YearMonthDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
}

fun dateAt(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long =
    Calendar.getInstance().apply {
        clear()
        set(year, month, day, hour, minute)
    }.timeInMillis
