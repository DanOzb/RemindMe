package com.example.remindme.triggers

import com.example.remindme.data.RepeatRule
import com.example.remindme.data.RepeatUnit
import com.example.remindme.data.TaskEntity
import com.example.remindme.data.dateAt
import com.example.remindme.data.epochDayOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

/** "Now" is Friday 6 Nov 2026, 12:00 in Stockholm. Months are 0-based (10 = November). */
class TriggerPlanTest {

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

    private val today get() = epochDayOf(2026, 10, 6)
    private fun at(h: Int, m: Int = 0, day: Int = 6) = dateAt(2026, 10, day, h, m)

    private fun task(
        place: Boolean = false,
        away: Boolean = false,
        either: Boolean = false,
        repeats: Boolean = false,
        dueDate: Long? = null,
        time: Int? = null,
        windowEnd: Int? = null,
        until: Long? = null,
        rule: RepeatRule? = null,
        lastFiredAt: Long? = null,
        completed: Boolean = false,
    ) = TaskEntity(
        id = 1, title = "t", createdAt = now - 1000, completed = completed,
        placeName = if (place) "Shop" else null,
        latitude = if (place) 59.33 else null, longitude = if (place) 18.06 else null,
        radiusMeters = if (place) 200 else null,
        awayFromPlace = away, matchEither = either, repeats = repeats,
        dueDate = dueDate, dueTimeMinutes = time, windowEndMinutes = windowEnd, untilDate = until,
        repeatRule = rule, lastFiredAt = lastFiredAt,
    )

    @Test
    fun a_place_reminder_gets_a_plain_geofence_and_no_alarm() {
        val plan = task(place = true).triggerPlan(now)
        assertEquals(GeofencePlan(59.33, 18.06, 200, exit = false, initialTrigger = false), plan.geofence)
        assertNull(plan.alarm)
    }

    @Test
    fun away_from_a_place_listens_for_leaving() {
        assertTrue(task(place = true, away = true).triggerPlan(now).geofence!!.exit)
    }

    @Test
    fun a_done_reminder_has_nothing_listening() {
        assertEquals(TriggerPlan.None, task(place = true, time = 18 * 60, completed = true).triggerPlan(now))
    }

    @Test
    fun a_time_today_fires_at_that_time() {
        assertEquals(AlarmPlan(at(18), AlarmKind.Fire), task(dueDate = today, time = 18 * 60).triggerPlan(now).alarm)
    }

    @Test
    fun a_time_without_a_date_that_has_passed_today_fires_tomorrow() {
        assertEquals(AlarmPlan(at(9, day = 7), AlarmKind.Fire), task(time = 9 * 60).triggerPlan(now).alarm)
    }

    @Test
    fun a_one_off_whose_time_passed_unseen_expires_now() {
        assertEquals(AlarmPlan(now, AlarmKind.Expire), task(dueDate = today, time = 9 * 60).triggerPlan(now).alarm)
    }

    @Test
    fun an_open_between_window_fires_now_once() {
        val open = task(time = 11 * 60, windowEnd = 13 * 60)
        assertEquals(AlarmPlan(now, AlarmKind.Fire), open.triggerPlan(now).alarm)
        val fired = open.copy(lastFiredAt = at(11, 30))
        assertEquals(AlarmPlan(at(11, day = 7), AlarmKind.Fire), fired.triggerPlan(now).alarm)
    }

    @Test
    fun a_daily_repeat_that_fired_today_waits_for_tomorrow() {
        val t = task(time = 8 * 60, rule = RepeatRule(unit = RepeatUnit.Day), lastFiredAt = at(8))
        assertEquals(AlarmPlan(at(8, day = 7), AlarmKind.Fire), t.triggerPlan(now).alarm)
    }

    @Test
    fun a_finished_series_expires() {
        val t = task(dueDate = epochDayOf(2026, 10, 1), time = 8 * 60, until = epochDayOf(2026, 10, 3), rule = RepeatRule(unit = RepeatUnit.Day))
        assertEquals(AlarmKind.Expire, t.triggerPlan(now).alarm!!.kind)
    }

    @Test
    fun either_listens_at_the_place_and_fires_at_the_time() {
        val plan = task(place = true, either = true, dueDate = today, time = 18 * 60).triggerPlan(now)
        assertEquals(false, plan.geofence!!.initialTrigger)
        assertEquals(AlarmPlan(at(18), AlarmKind.Fire), plan.alarm)
    }

    @Test
    fun both_before_the_window_only_waits_for_it_to_open() {
        val plan = task(place = true, dueDate = today, time = 17 * 60, windowEnd = 19 * 60).triggerPlan(now)
        assertNull(plan.geofence)
        assertEquals(AlarmPlan(at(17), AlarmKind.Resync), plan.alarm)
    }

    @Test
    fun both_inside_the_last_window_listens_and_expires_when_it_closes() {
        val plan = task(place = true, dueDate = today, time = 11 * 60, windowEnd = 13 * 60).triggerPlan(now)
        assertEquals(true, plan.geofence!!.initialTrigger)
        assertEquals(AlarmPlan(at(13), AlarmKind.Expire), plan.alarm)
    }

    @Test
    fun both_inside_a_daily_window_resyncs_when_it_closes() {
        val plan = task(place = true, time = 11 * 60, windowEnd = 13 * 60).triggerPlan(now)
        assertNotNull(plan.geofence)
        assertEquals(AlarmPlan(at(13), AlarmKind.Resync), plan.alarm)
    }

    @Test
    fun both_with_an_exact_time_counts_until_the_end_of_that_day() {
        val plan = task(place = true, dueDate = today, time = 9 * 60).triggerPlan(now)
        assertNotNull("9:00 has passed, but it's still today", plan.geofence)
        assertEquals(AlarmPlan(dateAt(2026, 10, 7, 0, 0), AlarmKind.Expire), plan.alarm)
    }

    @Test
    fun both_after_the_last_window_expires_now() {
        val plan = task(place = true, dueDate = epochDayOf(2026, 10, 5), time = 9 * 60).triggerPlan(now)
        assertNull(plan.geofence)
        assertEquals(AlarmPlan(now, AlarmKind.Expire), plan.alarm)
    }

    @Test
    fun a_repeating_both_that_fired_this_window_stops_listening_until_the_next() {
        val t = task(place = true, repeats = true, time = 11 * 60, windowEnd = 13 * 60, lastFiredAt = at(11, 15))
        val plan = t.triggerPlan(now)
        assertNull(plan.geofence)
        assertEquals(AlarmPlan(at(13), AlarmKind.Resync), plan.alarm)
    }

    @Test
    fun a_geofence_event_must_match_arrive_or_leave() {
        assertTrue(task(place = true).shouldFireOnGeofence(exit = false, now))
        assertFalse(task(place = true).shouldFireOnGeofence(exit = true, now))
        assertTrue(task(place = true, away = true).shouldFireOnGeofence(exit = true, now))
    }

    @Test
    fun every_time_ignores_quick_re_entries() {
        val t = task(place = true, repeats = true)
        assertFalse(t.copy(lastFiredAt = now - 60_000).shouldFireOnGeofence(false, now))
        assertTrue(t.copy(lastFiredAt = now - GEOFENCE_COOLDOWN_MS).shouldFireOnGeofence(false, now))
    }

    @Test
    fun both_ignores_a_late_event_once_the_window_has_closed() {
        val t = task(place = true, dueDate = today, time = 9 * 60, windowEnd = 11 * 60)
        assertFalse(t.shouldFireOnGeofence(exit = false, now))
        assertTrue(t.shouldFireOnGeofence(exit = false, at(10)))
    }

    @Test
    fun both_fires_at_most_once_per_window() {
        val t = task(place = true, repeats = true, time = 11 * 60, windowEnd = 13 * 60)
        assertTrue(t.shouldFireOnGeofence(false, now))
        assertFalse(t.copy(lastFiredAt = at(11, 30)).shouldFireOnGeofence(false, now))
    }

    @Test
    fun a_past_one_off_date_cant_be_switched_back_on_but_a_time_or_place_can() {
        assertFalse(task(dueDate = epochDayOf(2026, 10, 2), time = 9 * 60, completed = true).canReactivate(now))
        assertTrue(task(time = 9 * 60, completed = true, lastFiredAt = at(9)).canReactivate(now))
        assertTrue(task(place = true, completed = true).canReactivate(now))
    }

    @Test
    fun a_fire_alarm_is_not_repeated_for_the_same_occurrence() {
        val t = task(time = 18 * 60, rule = RepeatRule(unit = RepeatUnit.Day))
        assertTrue(t.shouldFireAlarm(at(18)))
        assertFalse(t.copy(lastFiredAt = at(18)).shouldFireAlarm(at(18)))
    }
}
