package com.example.remindme.data

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLng(val latitude: Double, val longitude: Double)

data class TaskWithDistance(
    val task: TaskEntity,
    val distanceMeters: Double?,
    val nextDue: Occurrence? = null,
)

private const val EARTH_RADIUS_METERS = 6_371_008.8

/** Pure Kotlin rather than Location.distanceBetween, so it runs in plain JVM unit tests. */
fun distanceMeters(a: LatLng, b: LatLng): Double {
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLng = Math.toRadians(b.longitude - a.longitude)
    val h = sin(dLat / 2).let { it * it } +
        cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) *
        sin(dLng / 2).let { it * it }
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(h.coerceAtMost(1.0)))
}

fun TaskEntity.distanceFrom(here: LatLng?): Double? {
    if (here == null || latitude == null || longitude == null) return null
    return distanceMeters(here, LatLng(latitude, longitude))
}

private val activeOrder = compareBy<TaskWithDistance>(
    { it.distanceMeters == null },
    { it.distanceMeters },
    { it.nextDue == null },
    { it.nextDue?.sortKey },
).thenByDescending { it.task.createdAt }

fun orderActive(
    tasks: List<TaskEntity>,
    here: LatLng?,
    now: Long = System.currentTimeMillis(),
): List<TaskWithDistance> =
    tasks.map { TaskWithDistance(it, it.distanceFrom(here), it.nextDue(now)) }.sortedWith(activeOrder)

fun orderCompleted(tasks: List<TaskEntity>): List<TaskEntity> =
    tasks.sortedWith(
        compareBy<TaskEntity> { it.completedAt == null }
            .thenByDescending { it.completedAt }
            .thenByDescending { it.createdAt }
    )
