package com.example.remindme.triggers

import com.example.remindme.data.Occurrence
import com.example.remindme.data.TaskEntity
import com.example.remindme.data.nextDue
import java.util.Calendar

/*
 * Pure decisions: given a reminder and "now", what should be listening for it, and what happens
 * when something fires. Android-free, so it's unit tested; Triggers.kt applies the results.
 *
 * - Place only: a geofence (arrive, or leave for "away from").
 * - Time only: an alarm at each occurrence. A Between window that's already open fires right away.
 * - Place OR time: both; whichever comes first.
 * - Place AND time: the geofence listens only while the time condition holds, checking at once whether
 *   you're already there. An exact time counts until the end of that day. When the last window
 *   closes without firing, the reminder expires (no "overdue" reminders).
 * - One-shot reminders turn inactive after firing; repeating ones record lastFiredAt and carry on.
 */

enum class AlarmKind {
    Fire,
    /** Its last chance has passed: "Missed" (for one-shots) and inactive. */
    Expire,
    /** A time window opens or closes: plan again. */
    Resync,
}

data class AlarmPlan(val at: Long, val kind: AlarmKind)

data class GeofencePlan(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int,
    /** Leaving instead of arriving ("away from"). */
    val exit: Boolean,
    /** Fire right away if already inside (or outside, for [exit]) when registered. */
    val initialTrigger: Boolean,
)

data class TriggerPlan(val geofence: GeofencePlan?, val alarm: AlarmPlan?) {
    companion object {
        val None = TriggerPlan(null, null)
    }
}

val TaskEntity.isOneShot: Boolean get() = repeatRule == null && !repeats

val TaskEntity.hasPlaceTrigger: Boolean get() = latitude != null && longitude != null && radiusMeters != null

val TaskEntity.hasTimeTrigger: Boolean get() = dueDate != null || dueTimeMinutes != null || repeatRule != null

/** Ignore re-entering within this long, so GPS jitter at the edge doesn't fire "every time" twice. */
const val GEOFENCE_COOLDOWN_MS = 10 * 60_000L

fun TaskEntity.triggerPlan(now: Long): TriggerPlan {
    if (completed) return TriggerPlan.None
    val place = if (hasPlaceTrigger) {
        GeofencePlan(latitude!!, longitude!!, radiusMeters!!, exit = awayFromPlace, initialTrigger = false)
    } else null

    if (!hasTimeTrigger) return TriggerPlan(place, null)
    if (place == null || matchEither) return TriggerPlan(place, timeAlarm(now))

    // Place AND time: listen at the place only while the time condition holds
    val window = windowAt(now, extendExactTimes = true)
    if (window != null) {
        val endKind = if (nextStart(window.start) == null) AlarmKind.Expire else AlarmKind.Resync
        val firedThisWindow = (lastFiredAt ?: Long.MIN_VALUE) >= window.start
        return TriggerPlan(
            geofence = if (firedThisWindow) null else place.copy(initialTrigger = true),
            alarm = AlarmPlan(window.spanEnd(extendExactTimes = true), endKind),
        )
    }
    val next = nextStart(now) ?: return TriggerPlan(null, AlarmPlan(now, AlarmKind.Expire))
    return TriggerPlan(null, AlarmPlan(next.start, AlarmKind.Resync))
}

fun TaskEntity.shouldFireOnGeofence(exit: Boolean, now: Long): Boolean {
    if (completed || !hasPlaceTrigger || exit != awayFromPlace) return false
    val last = lastFiredAt
    if (!hasTimeTrigger || matchEither) return last == null || now - last >= GEOFENCE_COOLDOWN_MS
    // Events can arrive late: check the window is still open, and fire at most once per window
    val window = windowAt(now, extendExactTimes = true) ?: return false
    return (last ?: Long.MIN_VALUE) < window.start
}

fun TaskEntity.shouldFireAlarm(at: Long): Boolean = !completed && (lastFiredAt ?: Long.MIN_VALUE) < at

/**
 * Whether switching a done reminder back on would leave it something to wait for. A one-off date
 * in the past wouldn't: it would expire straight away, so the user should pick a new date instead.
 */
fun TaskEntity.canReactivate(now: Long): Boolean {
    val alarm = copy(completed = false, completedAt = null, lastFiredAt = null).triggerPlan(now).alarm
    return !(alarm != null && alarm.kind == AlarmKind.Expire && alarm.at <= now)
}

private fun TaskEntity.timeAlarm(now: Long): AlarmPlan {
    val open = windowAt(now, extendExactTimes = false)
    if (open != null && (lastFiredAt ?: Long.MIN_VALUE) < open.start) return AlarmPlan(now, AlarmKind.Fire)
    // No next occurrence: a one-off whose time passed unseen (e.g. phone off), or a finished series
    val next = nextStart(now) ?: return AlarmPlan(now, AlarmKind.Expire)
    return AlarmPlan(next.start, AlarmKind.Fire)
}

private const val DAY_MS = 24 * 60 * 60_000L
private const val MAX_STEPS = 500

private fun TaskEntity.upcoming(t: Long): Occurrence? = nextDue(t)?.takeIf { it.end > t }

private fun TaskEntity.windowAt(now: Long, extendExactTimes: Boolean): Occurrence? {
    var t = now - 2 * DAY_MS // spans are at most a day (overnight windows reach into the next)
    repeat(MAX_STEPS) {
        val o = upcoming(t) ?: return null
        if (o.start > now) return null
        if (now < o.spanEnd(extendExactTimes)) return o
        t = if (o.end > t) o.end else t + 1
    }
    return null
}

private fun TaskEntity.nextStart(after: Long): Occurrence? {
    var t = after
    repeat(MAX_STEPS) {
        val o = upcoming(t) ?: return null
        if (o.start > after) return o
        t = if (o.end > t) o.end else t + 1
    }
    return null
}

/**
 * When an occurrence stops counting. Windows and all-day dates have a real end. An exact time has
 * none: combined with a place it counts until the end of that day ("at the shop, after 18:00");
 * on its own it's just that moment.
 */
private fun Occurrence.spanEnd(extendExactTimes: Boolean): Long = when {
    end > start -> end
    extendExactTimes -> endOfDay(start)
    else -> start
}

private fun endOfDay(millis: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis
