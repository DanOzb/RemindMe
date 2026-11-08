package com.example.remindme.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,

    val dueDate: Long? = null,          // LocalDate.toEpochDay()
    val dueTimeMinutes: Int? = null,    // window start, minutes after midnight
    val windowEndMinutes: Int? = null,
    val untilDate: Long? = null,        // epoch day, last day of a range

    val placeName: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Int? = null,
    val awayFromPlace: Boolean = false,

    val matchEither: Boolean = false,
    val repeats: Boolean = false,
    val repeatRule: RepeatRule? = null,
    val lastFiredAt: Long? = null,
)
