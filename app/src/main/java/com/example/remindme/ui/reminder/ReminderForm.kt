package com.example.remindme.ui.reminder

import androidx.compose.runtime.saveable.listSaver
import com.example.remindme.data.RepeatRule
import com.example.remindme.data.RepeatUnit
import com.example.remindme.data.place.PlaceResult
import com.example.remindme.ui.format.formatTimeOfDay
import java.util.Calendar

internal const val DefaultReminderHour = 9
internal const val DefaultReminderMinute = 0

internal fun defaultTimeFor(year: Int, month: Int, day: Int, now: Calendar = Calendar.getInstance()): Pair<Int, Int> {
    val isToday = now.get(Calendar.YEAR) == year && now.get(Calendar.MONTH) == month &&
        now.get(Calendar.DAY_OF_MONTH) == day
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val defaultMinutes = DefaultReminderHour * 60 + DefaultReminderMinute
    return when {
        !isToday || nowMinutes < defaultMinutes -> DefaultReminderHour to DefaultReminderMinute
        now.get(Calendar.HOUR_OF_DAY) < 23 -> now.get(Calendar.HOUR_OF_DAY) + 1 to 0
        else -> 23 to 59
    }
}

data class ReminderForm(
    val title: String = "",
    // Where. place is the editable label; the address and coordinates come from a picked search result
    val place: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val placeMode: PlaceMode = PlaceMode.Near,
    val radiusMeters: Int = 200,
    val timeMode: TimeMode = TimeMode.AnyTime,
    val hour: Int = DefaultReminderHour,
    val minute: Int = DefaultReminderMinute,
    val endHour: Int = 17,
    val endMinute: Int = 0,
    val dateMode: DateMode = DateMode.AnyDay,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH),
    val day: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val endYear: Int = year,
    val endMonth: Int = month,
    val endDay: Int = day,

    val match: MatchRule = MatchRule.Both,
    val repeat: RepeatMode = RepeatMode.Once,
    val customRepeat: RepeatRule = RepeatRule(unit = RepeatUnit.Week),
    val timeAutoFilled: Boolean = false,
) {
    /** Only a picked search result counts: a reminder can't fire for a place without coordinates. */
    val hasPlace: Boolean get() = latitude != null && longitude != null

    val placeLabel: String get() = place.trim().ifBlank { address.substringBefore(",").trim() }
    val hasTime: Boolean get() = timeMode != TimeMode.AnyTime
    val hasDate: Boolean get() = dateMode != DateMode.AnyDay
    val hasWhen: Boolean get() = hasTime || hasDate
    val hasBoth: Boolean get() = hasPlace && hasWhen

    val timeLabel: String get() = formatTimeOfDay(hour, minute)
    val endTimeLabel: String get() = formatTimeOfDay(endHour, endMinute)

    val startWeekday: Int
        get() = if (hasDate) calendarOf(year, month, day).get(Calendar.DAY_OF_WEEK)
        else Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    val effectiveRepeat: RepeatMode
        get() = if (repeat == RepeatMode.EveryTime && !hasPlace) RepeatMode.Once else repeat

    val repeatsEveryTime: Boolean get() = effectiveRepeat == RepeatMode.EveryTime

    val repeatRule: RepeatRule?
        get() = when (effectiveRepeat) {
            RepeatMode.Once, RepeatMode.EveryTime -> null
            RepeatMode.Daily -> RepeatRule(unit = RepeatUnit.Day)
            RepeatMode.Weekdays -> RepeatRule(unit = RepeatUnit.Week, days = RepeatRule.Weekdays)
            RepeatMode.Weekly -> RepeatRule(unit = RepeatUnit.Week, days = setOf(startWeekday))
            RepeatMode.Custom ->
                if (customRepeat.unit == RepeatUnit.Week && customRepeat.days.isEmpty()) customRepeat.copy(days = setOf(startWeekday))
                else customRepeat
        }

    val needsTime: Boolean
        get() = hasDate || (!hasPlace && repeatRule?.let { it.unit != RepeatUnit.Hour } == true)

    val timeOptions: List<TimeMode>
        get() = if (needsTime) listOf(TimeMode.At, TimeMode.Between) else TimeMode.entries

    private val timeWindowIsEmpty: Boolean
        get() = timeMode == TimeMode.Between && hour == endHour && minute == endMinute

    val isValid: Boolean get() = title.isNotBlank() && (hasPlace || hasWhen || repeatRule != null) && !timeWindowIsEmpty

    fun withTimeMode(mode: TimeMode): ReminderForm {
        val next = copy(timeMode = mode)
        return if (mode == TimeMode.Between && next.timeWindowIsEmpty) next.withEndTime((hour + 1) % 24, minute) else next
    }

    fun withDateMode(mode: DateMode): ReminderForm {
        var next = copy(dateMode = mode).withTimeFilledIn()
        if (mode == DateMode.Between && next.endDayKey <= next.startDayKey) {
            val end = calendarOf(year, month, day).apply { add(Calendar.DAY_OF_YEAR, 7) }
            next = next.copy(endYear = end.year, endMonth = end.month, endDay = end.dayOfMonth)
        }
        return next
    }

    fun withRepeat(mode: RepeatMode, custom: RepeatRule? = null): ReminderForm =
        copy(repeat = mode, customRepeat = custom ?: customRepeat).withTimeFilledIn()

    private fun withTimeFilledIn(): ReminderForm {
        if (!needsTime || timeMode != TimeMode.AnyTime) return this
        val (h, m) = if (hasDate) defaultTimeFor(year, month, day) else DefaultReminderHour to DefaultReminderMinute
        return copy(timeMode = TimeMode.At, hour = h, minute = m, timeAutoFilled = true)
    }

    fun withPlace(result: PlaceResult) = copy(
        place = result.label,
        address = result.address,
        latitude = result.latLng.latitude,
        longitude = result.latLng.longitude,
    )

    fun withoutPlace() = copy(place = "", address = "", latitude = null, longitude = null)

    fun withStartTime(hour: Int, minute: Int) = copy(hour = hour, minute = minute, timeAutoFilled = false)
    fun withEndTime(hour: Int, minute: Int) = copy(endHour = hour, endMinute = minute)

    /** Moving the start past the end drags the end along. */
    fun withStartDate(year: Int, month: Int, day: Int): ReminderForm {
        var next = copy(year = year, month = month, day = day)
        if (timeAutoFilled) {
            val (h, m) = defaultTimeFor(year, month, day)
            next = next.copy(hour = h, minute = m)
        }
        return if (next.endDayKey < next.startDayKey) next.copy(endYear = year, endMonth = month, endDay = day) else next
    }

    /** Moving the end before the start drags the start along. */
    fun withEndDate(year: Int, month: Int, day: Int): ReminderForm {
        val next = copy(endYear = year, endMonth = month, endDay = day)
        return if (next.endDayKey < next.startDayKey) next.withStartDate(year, month, day) else next
    }

    private val startDayKey: Int get() = dayKey(year, month, day)
    private val endDayKey: Int get() = dayKey(endYear, endMonth, endDay)
}

private fun dayKey(year: Int, month: Int, day: Int) = year * 10_000 + month * 100 + day

private fun calendarOf(year: Int, month: Int, day: Int): Calendar =
    Calendar.getInstance().apply { clear(); set(year, month, day) }

private val Calendar.year get() = get(Calendar.YEAR)
private val Calendar.month get() = get(Calendar.MONTH)
private val Calendar.dayOfMonth get() = get(Calendar.DAY_OF_MONTH)

internal val ReminderFormSaver = listSaver<ReminderForm, Any?>(
    save = {
        listOf(
            it.title, it.place, it.placeMode.name, it.radiusMeters,
            it.timeMode.name, it.hour, it.minute, it.endHour, it.endMinute,
            it.dateMode.name, it.year, it.month, it.day, it.endYear, it.endMonth, it.endDay,
            it.match.name, it.repeat.name, it.timeAutoFilled,
            it.customRepeat.every, it.customRepeat.unit.name, ArrayList(it.customRepeat.days),
            it.address, it.latitude, it.longitude,
        )
    },
    restore = {
        ReminderForm(
            title = it[0] as String,
            place = it[1] as String,
            placeMode = PlaceMode.valueOf(it[2] as String),
            radiusMeters = it[3] as Int,
            timeMode = TimeMode.valueOf(it[4] as String),
            hour = it[5] as Int,
            minute = it[6] as Int,
            endHour = it[7] as Int,
            endMinute = it[8] as Int,
            dateMode = DateMode.valueOf(it[9] as String),
            year = it[10] as Int,
            month = it[11] as Int,
            day = it[12] as Int,
            endYear = it[13] as Int,
            endMonth = it[14] as Int,
            endDay = it[15] as Int,
            match = MatchRule.valueOf(it[16] as String),
            repeat = RepeatMode.valueOf(it[17] as String),
            timeAutoFilled = it[18] as Boolean,
            customRepeat = RepeatRule(
                every = it[19] as Int,
                unit = RepeatUnit.valueOf(it[20] as String),
                days = (it[21] as List<*>).filterIsInstance<Int>().toSet(),
            ),
            address = it[22] as String,
            latitude = it[23] as Double?,
            longitude = it[24] as Double?,
        )
    }
)
