package com.example.remindme.triggers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/** Exact when the user allowed "Alarms & reminders", otherwise Android may deliver a few minutes late. */
class Alarms(context: Context) {

    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean = appContext.canScheduleExactAlarms()

    fun schedule(taskId: Int, plan: AlarmPlan) {
        val intent = baseIntent()
            .putExtra(TriggerReceiver.EXTRA_TASK_ID, taskId)
            .putExtra(TriggerReceiver.EXTRA_KIND, plan.kind.name)
            .putExtra(TriggerReceiver.EXTRA_AT, plan.at)
        val pending = PendingIntent.getBroadcast(
            appContext, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // AllowWhileIdle: still delivered in Doze
        try {
            if (canScheduleExact()) manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plan.at, pending)
            else manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plan.at, pending)
        } catch (_: SecurityException) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plan.at, pending) // permission revoked meanwhile
        }
    }

    fun cancel(taskId: Int) {
        // Extras don't count when matching, so the bare intent finds the scheduled one
        val pending = PendingIntent.getBroadcast(
            appContext, taskId, baseIntent(), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        manager.cancel(pending)
        pending.cancel()
    }

    private fun baseIntent() = Intent(appContext, TriggerReceiver::class.java).setAction(TriggerReceiver.ACTION_ALARM)
}

fun Context.canScheduleExactAlarms(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
