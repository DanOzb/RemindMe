package com.example.remindme.ui.format

import java.text.DateFormat
import java.util.Calendar
import java.util.Date

fun formatTime(millis: Long): String =
    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(millis))

fun formatTimeOfDay(hour: Int, minute: Int): String =
    formatTime(
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.timeInMillis
    )
