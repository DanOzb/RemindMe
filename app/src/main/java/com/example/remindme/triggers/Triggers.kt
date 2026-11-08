package com.example.remindme.triggers

import android.content.Context
import com.example.remindme.data.AppDatabase
import com.example.remindme.data.TaskEntity
import com.example.remindme.data.TaskRepository
import com.example.remindme.notifications.ReminderNotifications

class Triggers(context: Context) {

    private val appContext = context.applicationContext
    private val repository = TaskRepository(AppDatabase.getInstance(appContext).taskDao())
    private val notifications = ReminderNotifications(appContext)
    private val geofences = Geofences(appContext)
    private val alarms = Alarms(appContext)

    suspend fun sync(taskId: Int) {
        val task = repository.get(taskId)
        if (task == null || task.completed) clear(taskId) else sync(task)
    }

    /**
     * Every active reminder. Geofences and alarms are wiped by reboots, app updates and some
     * settings changes, and permissions may have changed, so this rebuilds from the database.
     */
    suspend fun syncAll() {
        var geofencesUsed = 0
        repository.getActive().forEach { task ->
            if (sync(task, allowGeofence = geofencesUsed < MAX_GEOFENCES)) geofencesUsed++
        }
    }

    suspend fun clear(taskId: Int, cancelNotification: Boolean = false) {
        geofences.remove(taskId)
        alarms.cancel(taskId)
        if (cancelNotification) notifications.cancel(taskId)
    }

    suspend fun onGeofence(taskIds: List<Int>, exit: Boolean) {
        val now = System.currentTimeMillis()
        repository.getActiveByIds(taskIds)
            .filter { it.shouldFireOnGeofence(exit, now) }
            .forEach { fire(it) }
    }

    /** An alarm went off. [at] is the moment it was planned for. */
    suspend fun onAlarm(taskId: Int, kind: AlarmKind, at: Long) {
        val task = repository.get(taskId)?.takeUnless { it.completed } ?: return
        when (kind) {
            AlarmKind.Fire -> if (task.shouldFireAlarm(at)) fire(task) else sync(task)
            AlarmKind.Expire -> expire(task)
            AlarmKind.Resync -> sync(task)
        }
    }

    /** Returns whether a geofence was registered (they count towards Android's limit). */
    private suspend fun sync(task: TaskEntity, allowGeofence: Boolean = true): Boolean {
        val now = System.currentTimeMillis()
        val plan = task.triggerPlan(now)

        val fence = plan.geofence?.takeIf { allowGeofence }
        val registered = if (fence != null) geofences.register(task.id, fence) else {
            geofences.remove(task.id)
            false
        }

        val alarm = plan.alarm
        when {
            alarm == null -> alarms.cancel(task.id)
            // Due already (a window that's open, or a time that passed): act now instead of scheduling
            alarm.at <= now -> {
                alarms.cancel(task.id)
                when (alarm.kind) {
                    AlarmKind.Fire -> fire(task)
                    AlarmKind.Expire -> expire(task)
                    AlarmKind.Resync -> Unit // we're already re-planning
                }
            }
            else -> alarms.schedule(task.id, alarm)
        }
        return registered
    }

    private suspend fun fire(task: TaskEntity) {
        notifications.showReminder(task, markDone = !task.isOneShot)
        if (task.isOneShot) {
            repository.setCompleted(task.id, true)
            clear(task.id)
        } else {
            repository.markFired(listOf(task.id))
            sync(task.id) // plans the next occurrence, with lastFiredAt now set
        }
    }

    /** Its last chance passed. One-shots say so; a finished series just ends quietly. */
    private suspend fun expire(task: TaskEntity) {
        if (task.isOneShot) notifications.showMissed(task)
        repository.setCompleted(task.id, true)
        clear(task.id)
    }

    private companion object {
        const val MAX_GEOFENCES = 100
    }
}
