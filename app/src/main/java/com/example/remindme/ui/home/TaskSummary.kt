package com.example.remindme.ui.home

import com.example.remindme.data.TaskWithDistance
import com.example.remindme.data.nextDue
import com.example.remindme.ui.format.formatDue

fun TaskWithDistance.whereAndWhen(): String {
    val where = task.placeName?.let { if (task.awayFromPlace) "Away from $it" else it }
    // Completed tasks and previews come without nextDue, so work it out here
    val due = (nextDue ?: task.nextDue())?.let { formatDue(it.start, it.allDay) }
    return listOfNotNull(where, due).joinToString(" · ")
}
