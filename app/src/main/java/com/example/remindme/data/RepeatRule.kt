package com.example.remindme.data

import kotlinx.serialization.Serializable
import java.util.Calendar

/** Stored by name in the database (inside RepeatRule's JSON), so don't rename these. */
enum class RepeatUnit { Hour, Day, Week, Month, Year }

@Serializable
data class RepeatRule(
    val every: Int = 1,
    val unit: RepeatUnit = RepeatUnit.Day,
    val days: Set<Int> = emptySet(),
) {
    companion object {
        val Weekdays = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)
    }
}
