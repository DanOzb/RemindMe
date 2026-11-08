package com.example.remindme.ui.reminder

import com.example.remindme.data.LatLng
import com.example.remindme.data.RepeatRule
import com.example.remindme.data.RepeatUnit
import com.example.remindme.data.place.PlaceResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class RepeatTest {

    @Before fun englishWeekStartingMonday() = Locale.setDefault(Locale.UK)

    // Thursday, Nov 5 2026
    private val home = PlaceResult("Home", "Rädisvägen 12, Stockholm", LatLng(59.33, 18.06))

    private val form = ReminderForm(title = "Pay rent", dateMode = DateMode.On, year = 2026, month = Calendar.NOVEMBER, day = 5)

    private fun describe(rule: RepeatRule) = describeRepeat(rule, form)

    @Test fun `describes hours and days`() {
        assertEquals("every hour", describe(RepeatRule(1, RepeatUnit.Hour)))
        assertEquals("every 3 hours", describe(RepeatRule(3, RepeatUnit.Hour)))
        assertEquals("every 2 days", describe(RepeatRule(2, RepeatUnit.Day)))
    }

    @Test fun `describes weeks`() {
        assertEquals("every weekday", describe(RepeatRule(1, RepeatUnit.Week, RepeatRule.Weekdays)))
        assertEquals("every Thursday", describe(RepeatRule(1, RepeatUnit.Week, setOf(Calendar.THURSDAY))))
        assertEquals("every Mon, Wed and Fri", describe(RepeatRule(1, RepeatUnit.Week, setOf(Calendar.FRIDAY, Calendar.MONDAY, Calendar.WEDNESDAY))))
        assertEquals("every 2 weeks on Mon and Sun", describe(RepeatRule(2, RepeatUnit.Week, setOf(Calendar.SUNDAY, Calendar.MONDAY))))
    }

    @Test fun `describes months with an ordinal day`() {
        assertEquals("every month on the 5th", describe(RepeatRule(1, RepeatUnit.Month)))
        assertEquals("every 3 months on the 21st", describeRepeat(RepeatRule(3, RepeatUnit.Month), form.copy(day = 21)))
        assertEquals("every month on the 12th", describeRepeat(RepeatRule(1, RepeatUnit.Month), form.copy(day = 12)))
    }

    @Test fun `weekly preset uses the start date's weekday`() =
        assertEquals(setOf(Calendar.THURSDAY), form.withRepeat(RepeatMode.Weekly).repeatRule?.days)

    @Test fun `every time without a place is just once`() {
        val f = form.withRepeat(RepeatMode.EveryTime)
        assertEquals(RepeatMode.Once, f.effectiveRepeat)
        assertNull(f.repeatRule)
        assertTrue(f.withPlace(home).repeatsEveryTime)
    }

    @Test fun `a daily schedule fills in a time, an hourly one doesn't`() {
        val blank = ReminderForm(title = "Drink water")
        val daily = blank.withRepeat(RepeatMode.Daily)
        assertEquals(TimeMode.At, daily.timeMode)
        assertEquals("no date, so 9:00 even late in the day", DefaultReminderHour to 0, daily.hour to daily.minute)
        val hourly = blank.withRepeat(RepeatMode.Custom, RepeatRule(2, RepeatUnit.Hour))
        assertEquals(TimeMode.AnyTime, hourly.timeMode)
        assertTrue("an hourly repeat is enough on its own", hourly.isValid)
        assertFalse(blank.isValid)
    }

    @Test fun `a daily schedule with a place doesn't need a time`() =
        assertEquals(TimeMode.AnyTime, ReminderForm(title = "x").withPlace(home).withRepeat(RepeatMode.Daily).timeMode)

    @Test fun `saved schedules come back as the matching preset`() {
        fun roundTrip(f: ReminderForm) = f.toTask(null).toForm()
        assertEquals(RepeatMode.Weekdays, roundTrip(form.withRepeat(RepeatMode.Weekdays)).repeat)
        assertEquals(RepeatMode.Weekly, roundTrip(form.withRepeat(RepeatMode.Weekly)).repeat)
        val custom = RepeatRule(2, RepeatUnit.Week, setOf(Calendar.MONDAY, Calendar.WEDNESDAY))
        val back = roundTrip(form.withRepeat(RepeatMode.Custom, custom))
        assertEquals(RepeatMode.Custom, back.repeat)
        assertEquals(custom, back.repeatRule)
        assertEquals(RepeatMode.EveryTime, roundTrip(form.withPlace(home).withRepeat(RepeatMode.EveryTime)).repeat)
    }
}
