package com.example.remindme.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderTasksTest {

    private val t0 = 1_700_000_000_000L
    private val day = 24 * 60 * 60 * 1000L

    // Stockholm Centralstation, and points roughly 150 m, 900 m and 3.2 km north of it.
    private val here = LatLng(59.3301, 18.0586)
    private fun north(meters: Double) = here.latitude + meters / 111_195.0

    private fun placed(id: Int, metersNorth: Double, dueDate: Long? = null) = TaskEntity(
        id = id,
        title = "Task $id",
        placeName = "Place $id",
        latitude = north(metersNorth),
        longitude = here.longitude,
        radiusMeters = 200,
        dueDate = dueDate,
    )

    private fun ids(tasks: List<TaskEntity>, at: LatLng? = here) = orderActive(tasks, at).map { it.task.id }

    @Test
    fun the_closer_place_comes_first() {
        val far = placed(1, metersNorth = 3200.0)
        val near = placed(2, metersNorth = 150.0)
        val middle = placed(3, metersNorth = 900.0)

        assertEquals(listOf(2, 3, 1), ids(listOf(far, near, middle)))
    }

    @Test
    fun distance_beats_due_date() {
        val farButDueToday = placed(1, metersNorth = 3200.0, dueDate = 0)
        val nearButDueLater = placed(2, metersNorth = 150.0, dueDate = 10)

        assertEquals(listOf(2, 1), ids(listOf(farButDueToday, nearButDueLater)))
    }

    @Test
    fun tasks_without_a_place_come_after_placed_ones() {
        val noPlace = TaskEntity(id = 1, title = "Call Mum", dueDate = 0)
        val placed = placed(2, metersNorth = 3200.0)

        assertEquals(listOf(2, 1), ids(listOf(noPlace, placed)))
    }

    @Test
    fun unknown_location_falls_back_to_due_order() {
        val later = placed(1, metersNorth = 150.0, dueDate = 10)
        val sooner = placed(2, metersNorth = 3200.0, dueDate = 5)

        assertEquals(listOf(2, 1), ids(listOf(later, sooner), at = null))
        assertNull(orderActive(listOf(later), null).single().distanceMeters)
    }

    @Test
    fun the_earlier_due_date_wins_then_the_earlier_time() {
        val tomorrow = TaskEntity(id = 1, title = "Dinner", dueDate = 11, dueTimeMinutes = 60)
        val todayLate = TaskEntity(id = 2, title = "Pharmacy", dueDate = 10, dueTimeMinutes = 20 * 60)
        val todayEarly = TaskEntity(id = 3, title = "Gym", dueDate = 10, dueTimeMinutes = 8 * 60)

        assertEquals(listOf(3, 2, 1), ids(listOf(tomorrow, todayLate, todayEarly)))
    }

    @Test
    fun a_dated_task_comes_before_an_undated_one() {
        val undated = TaskEntity(id = 1, title = "Water the plants")
        val dated = TaskEntity(id = 2, title = "Call Mum", dueDate = 10)

        assertEquals(listOf(2, 1), ids(listOf(undated, dated)))
    }

    @Test
    fun the_newest_wins_when_nothing_else_distinguishes_two_tasks() {
        val older = TaskEntity(id = 1, title = "Water the plants", createdAt = t0 - day)
        val newer = TaskEntity(id = 2, title = "Call Mum", createdAt = t0)

        assertEquals(listOf(2, 1), ids(listOf(older, newer)))
    }

    @Test
    fun the_computed_distance_is_carried_along() {
        val d = orderActive(listOf(placed(1, metersNorth = 150.0)), here).single().distanceMeters!!

        assertEquals(150.0, d, 1.0)
    }

    @Test
    fun an_empty_list_stays_empty() {
        assertEquals(emptyList<TaskWithDistance>(), orderActive(emptyList(), here))
    }
}
