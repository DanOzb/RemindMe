package com.example.remindme.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.remindme.data.AppDatabase
import com.example.remindme.data.TaskRepository
import com.example.remindme.triggers.Triggers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MARK_DONE) return
        val taskId = intent.getIntExtra(ReminderNotifications.EXTRA_REMINDER_ID, 0).takeIf { it > 0 } ?: return

        // goAsync keeps the process alive until the database write finishes
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TaskRepository(AppDatabase.getInstance(context).taskDao()).setCompleted(taskId, true)
                Triggers(context).clear(taskId, cancelNotification = true)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_DONE = "com.example.remindme.MARK_DONE"
    }
}
