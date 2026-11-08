package com.example.remindme.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskStatusTest {

    private fun task(distanceMeters: Double?, radiusMeters: Int?, completed: Boolean = false) = TaskWithDistance(
        task = TaskEntity(
            id = 1,
            title = "Buy apples",
            completed = completed,
            placeName = "Green Basket",
            radiusMeters = radiusMeters,
        ),
        distanceMeters = distanceMeters,
    )

    @Test
    fun inside_the_radius_counts_as_in_range() {
        assertTrue(task(distanceMeters = 150.0, radiusMeters = 200).isInRange)
    }

    @Test
    fun sitting_exactly_on_the_radius_counts_as_in_range() {
        assertTrue(task(distanceMeters = 200.0, radiusMeters = 200).isInRange)
    }

    @Test
    fun beyond_the_radius_is_not_in_range() {
        assertFalse(task(distanceMeters = 900.0, radiusMeters = 200).isInRange)
    }

    @Test
    fun no_distance_reading_yet_is_not_in_range() {
        assertFalse(task(distanceMeters = null, radiusMeters = 200).isInRange)
    }

    @Test
    fun a_place_without_a_radius_is_not_in_range() {
        assertFalse(task(distanceMeters = 150.0, radiusMeters = null).isInRange)
    }

    @Test
    fun being_nearby_makes_an_unfinished_task_active_now() {
        assertTrue(task(distanceMeters = 150.0, radiusMeters = 200).isActiveNow)
    }

    @Test
    fun a_completed_task_is_never_active_however_close_it_is() {
        assertFalse(task(distanceMeters = 150.0, radiusMeters = 200, completed = true).isActiveNow)
    }
}
