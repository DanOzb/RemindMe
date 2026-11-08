package com.example.remindme.triggers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val triggers = Triggers(context)
        // goAsync keeps the process alive until the database work finishes
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_GEOFENCE -> {
                        val event = GeofencingEvent.fromIntent(intent) ?: return@launch
                        if (event.hasError()) return@launch
                        val ids = event.triggeringGeofences.orEmpty().mapNotNull { it.requestId.toIntOrNull() }
                        triggers.onGeofence(ids, exit = event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
                    }
                    ACTION_ALARM -> {
                        val taskId = intent.getIntExtra(EXTRA_TASK_ID, 0).takeIf { it > 0 } ?: return@launch
                        val kind = intent.getStringExtra(EXTRA_KIND)
                            ?.let { name -> AlarmKind.entries.find { it.name == name } } ?: return@launch
                        triggers.onAlarm(taskId, kind, intent.getLongExtra(EXTRA_AT, 0L))
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_GEOFENCE = "com.example.remindme.GEOFENCE"
        const val ACTION_ALARM = "com.example.remindme.ALARM"
        const val EXTRA_TASK_ID = "com.example.remindme.TASK_ID"
        const val EXTRA_KIND = "com.example.remindme.KIND"
        const val EXTRA_AT = "com.example.remindme.AT"
    }
}

/**
 * System events that wipe or invalidate triggers: reboot and app update wipe geofences and alarms;
 * a clock or time zone change moves local times; the exact-alarm permission changes how alarms are set.
 */
class SystemEventsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Triggers(context).syncAll()
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        val HANDLED = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
        )
    }
}
