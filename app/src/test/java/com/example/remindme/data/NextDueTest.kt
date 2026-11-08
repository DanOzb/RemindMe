package com.example.remindme.data

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

/** "Now" is Friday 6 Nov 2026, 12:00 in Stockholm. Months are 0-based (10 = November). */
class NextDueTest {

    private lateinit var savedZone: TimeZone
    private var now = 0L

    @Before
    fun setUp() {
        savedZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"))
        now = dateAt(2026, 10, 6, 12, 0)
    }

    @After
    fun tearDown() = TimeZone.setDefault(savedZone)

    private fun task(
        dueDate: Long? = null,
        time: Int? = null,
        windowEnd: Int? = null,
        until: Long? = null,
        rule: RepeatRule? = null,
        createdAt: Long = now,
    ) = TaskEntity(
        title = "t", createdAt = createdAt, dueDate = dueDate, dueTimeMinutes = time,
        windowEndMinutes = windowEnd, untilDate = until, repeatRule = rule,
    )

    private fun day(month: Int, day: Int, year: Int = 2026) = epochDayOf(year, month, day)
    private fun startOf(t: TaskEntity) = t.nextDue(now)?.start

    @Test
    fun no_date_or_time_has_no_next_due() {
        assertNull(task().nextDue(now))
    }

    @Test
    fun a_past_one_off_stays_due_at_its_time_so_it_sorts_as_overdue() {
        assertEquals(dateAt(2026, 10, 2, 9, 0), startOf(task(dueDate = day(10, 2), time = 9 * 60)))
    }

    @Test
    fun a_time_without_a_date_is_today_if_still_ahead_otherwise_tomorrow() {
        assertEquals(dateAt(2026, 10, 6, 18, 0), startOf(task(time = 18 * 60)))
        assertEquals(dateAt(2026, 10, 7, 9, 0), startOf(task(time = 9 * 60)))
    }

    @Test
    fun an_open_time_window_counts_as_today() {
        assertEquals(dateAt(2026, 10, 6, 11, 0), startOf(task(time = 11 * 60, windowEnd = 13 * 60)))
    }

    @Test
    fun a_daily_repeat_that_started_a_month_ago_is_due_tomorrow_not_a_month_ago() {
        val t = task(dueDate = day(9, 6), time = 8 * 60, rule = RepeatRule(unit = RepeatUnit.Day))
        assertEquals(dateAt(2026, 10, 7, 8, 0), startOf(t))
    }

    @Test
    fun a_weekly_repeat_picks_the_next_chosen_weekday() {
        val monWed = RepeatRule(unit = RepeatUnit.Week, days = setOf(2, 4)) // Calendar.MONDAY, WEDNESDAY
        assertEquals(dateAt(2026, 10, 9, 8, 0), startOf(task(dueDate = day(10, 2), time = 8 * 60, rule = monWed)))
    }

    @Test
    fun every_other_week_skips_the_off_week() {
        val fridays = RepeatRule(every = 2, unit = RepeatUnit.Week, days = setOf(6)) // Calendar.FRIDAY
        // Started Fri 30 Oct, so today (6 Nov) is an off week
        assertEquals(dateAt(2026, 10, 13, 18, 0), startOf(task(dueDate = day(9, 30), time = 18 * 60, rule = fridays)))
    }

    @Test
    fun a_monthly_repeat_on_the_31st_falls_on_the_last_day_of_shorter_months() {
        val t = task(dueDate = day(0, 31), time = 9 * 60, rule = RepeatRule(unit = RepeatUnit.Month))
        assertEquals(dateAt(2026, 10, 30, 9, 0), startOf(t))
    }

    @Test
    fun a_yearly_repeat_on_29_february_uses_the_28th_in_other_years() {
        val t = task(dueDate = day(1, 29, year = 2024), time = 9 * 60, rule = RepeatRule(unit = RepeatUnit.Year))
        assertEquals(dateAt(2027, 1, 28, 9, 0), startOf(t))
    }

    @Test
    fun a_date_range_repeats_daily_until_its_last_day() {
        assertEquals(dateAt(2026, 10, 7, 8, 0), startOf(task(dueDate = day(10, 1), time = 8 * 60, until = day(10, 20))))
    }

    @Test
    fun a_finished_date_range_sorts_as_overdue_on_its_last_day() {
        assertEquals(dateAt(2026, 10, 3, 8, 0), startOf(task(dueDate = day(10, 1), time = 8 * 60, until = day(10, 3))))
    }

    @Test
    fun a_repeat_that_starts_later_is_due_on_its_start_date() {
        val t = task(dueDate = day(11, 1), time = 8 * 60, rule = RepeatRule(unit = RepeatUnit.Day))
        assertEquals(dateAt(2026, 11, 1, 8, 0), startOf(t))
    }

    @Test
    fun every_three_hours_around_the_clock() {
        val t = task(dueDate = day(10, 6), time = 8 * 60, rule = RepeatRule(every = 3, unit = RepeatUnit.Hour))
        assertEquals(dateAt(2026, 10, 6, 14, 0), startOf(t)) // 08, 11, then 14
    }

    @Test
    fun every_two_hours_inside_a_window_moves_to_the_next_day_once_the_window_is_used_up() {
        val t = task(time = 8 * 60, windowEnd = 11 * 60, rule = RepeatRule(every = 2, unit = RepeatUnit.Hour))
        assertEquals(dateAt(2026, 10, 7, 8, 0), startOf(t)) // 08 and 10 have passed
    }

    @Test
    fun all_day_sorts_after_timed_on_the_same_day() {
        val allDay = task(dueDate = day(10, 7)).copy(id = 1)
        val timed = task(dueDate = day(10, 7), time = 20 * 60).copy(id = 2)
        assertEquals(listOf(2, 1), orderActive(listOf(allDay, timed), null, now).map { it.task.id })
    }

    @Test
    fun a_time_only_reminder_today_beats_a_dated_one_tomorrow() {
        val tomorrow = task(dueDate = day(10, 7), time = 9 * 60).copy(id = 1)
        val tonight = task(time = 18 * 60).copy(id = 2)
        assertEquals(listOf(2, 1), orderActive(listOf(tomorrow, tonight), null, now).map { it.task.id })
    }

    @Test
    fun completed_tasks_sort_by_recency_with_unstamped_last() {
        val a = TaskEntity(id = 1, title = "a", completed = true, completedAt = 5)
        val b = TaskEntity(id = 2, title = "b", completed = true, completedAt = 9)
        val c = TaskEntity(id = 3, title = "c", completed = true, completedAt = null)
        assertEquals(listOf(2, 1, 3), orderCompleted(listOf(c, a, b)).map { it.id })
    }
}
