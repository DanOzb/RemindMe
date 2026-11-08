package com.example.remindme.ui.reminder

import com.example.remindme.data.RepeatRule
import com.example.remindme.data.RepeatUnit
import com.example.remindme.data.TaskEntity
import com.example.remindme.data.dateOfEpochDay
import com.example.remindme.data.epochDayOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderFormMappingTest {

    private fun roundTrip(task: TaskEntity) = task.toForm().toTask(task)

    @Test
    fun epoch_days_round_trip() {
        val day = epochDayOf(2026, 10, 6) // 6 Nov 2026, month is 0-based
        assertEquals(20_763L, day)        // LocalDate.of(2026, 11, 6).toEpochDay()
        assertEquals(2026, dateOfEpochDay(day).year)
        assertEquals(10, dateOfEpochDay(day).month)
        assertEquals(6, dateOfEpochDay(day).day)
    }

    @Test
    fun a_dated_timed_window_with_a_date_range_survives() {
        val task = TaskEntity(
            id = 7, title = "Gym", createdAt = 1L,
            dueDate = epochDayOf(2026, 10, 6), dueTimeMinutes = 8 * 60 + 15, windowEndMinutes = 10 * 60,
            untilDate = epochDayOf(2026, 10, 20),
        )
        assertEquals(task, roundTrip(task))
    }

    @Test
    fun a_place_reminder_keeps_its_coordinates() {
        val task = TaskEntity(
            id = 3, title = "Buy apples", createdAt = 1L,
            placeName = "Green Basket", address = "Rädisvägen 12, Stockholm",
            latitude = 59.33, longitude = 18.06, radiusMeters = 300,
            awayFromPlace = true, repeats = true,
        )
        assertEquals(task, roundTrip(task))
    }

    private val home = TaskEntity(
        id = 3, title = "x", placeName = "Home", address = "Rädisvägen 12, Stockholm",
        latitude = 59.33, longitude = 18.06, radiusMeters = 200,
    )

    @Test
    fun renaming_the_place_keeps_its_coordinates() {
        val saved = home.toForm().copy(place = "Gym").toTask(home)
        assertEquals("Gym", saved.placeName)
        assertEquals(59.33, saved.latitude!!, 0.0)
    }

    @Test
    fun a_blank_label_falls_back_to_the_address() {
        assertEquals("Rädisvägen 12", home.toForm().copy(place = " ").toTask(home).placeName)
    }

    @Test
    fun removing_the_place_clears_everything_about_it() {
        val saved = home.toForm().withoutPlace().toTask(home)
        assertNull(saved.placeName)
        assertNull(saved.address)
        assertNull(saved.latitude)
        assertNull(saved.radiusMeters)
    }

    @Test
    fun a_place_name_without_coordinates_is_not_a_place() {
        // Saved before search existed: it opens as a search query, and isn't kept unless re-picked
        val legacy = TaskEntity(id = 4, title = "x", placeName = "Green Basket", radiusMeters = 200)
        val form = legacy.toForm()
        assertEquals(false, form.hasPlace)
        assertEquals("Green Basket", form.place)
        assertNull(form.toTask(legacy).placeName)
    }

    @Test
    fun a_custom_repeat_survives() {
        val task = TaskEntity(
            id = 4, title = "Bins", createdAt = 1L, dueTimeMinutes = 7 * 60,
            repeatRule = RepeatRule(every = 2, unit = RepeatUnit.Week, days = setOf(2, 5)),
        )
        assertEquals(task, roundTrip(task))
    }

    @Test
    fun saving_re_arms_a_completed_or_fired_reminder() {
        val task = TaskEntity(id = 5, title = "Call Mum", completed = true, completedAt = 9L, lastFiredAt = 9L, dueTimeMinutes = 18 * 60)
        val saved = roundTrip(task)
        assertEquals(false, saved.completed)
        assertNull(saved.completedAt)
        assertNull(saved.lastFiredAt)
    }

    @Test
    fun a_new_reminder_gets_id_zero_so_room_assigns_one() {
        assertEquals(0, ReminderForm(title = "New").toTask(null).id)
    }
}
