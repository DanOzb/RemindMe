package com.example.remindme.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.remindme.MainActivity
import com.example.remindme.R
import com.example.remindme.data.TaskEntity
import com.example.remindme.ui.theme.TextBrandColor

class ReminderNotifications(context: Context) {

    private val context = context.applicationContext
    private val manager = NotificationManagerCompat.from(this.context)

    /** Safe to call repeatedly; called at app start. */
    fun createChannels() {
        manager.createNotificationChannelsCompat(
            listOf(
                NotificationChannelCompat.Builder(CHANNEL_REMINDERS, NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setName("Reminders")
                    .setDescription("When a reminder's place or time comes up")
                    .build(),
                NotificationChannelCompat.Builder(CHANNEL_MISSED, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                    .setName("Missed reminders")
                    .setDescription("When a reminder runs out of time before its conditions were met")
                    .build(),
            )
        )
    }

    /**
     * The reminder itself. Returns false when notifications are off. [markDone] adds the button;
     * one-shot reminders turn inactive when they fire, so they don't need it.
     */
    fun showReminder(task: TaskEntity, markDone: Boolean = true): Boolean =
        notify(task, CHANNEL_REMINDERS, title = task.title, text = reminderText(task), markDone = markDone)

    fun showMissed(task: TaskEntity): Boolean =
        notify(
            task, CHANNEL_MISSED,
            title = "Missed: ${task.title}",
            text = "Its time ran out before it could remind you, so it's been turned off.",
            markDone = false,
        )

    fun cancel(taskId: Int) = manager.cancel(taskId)

    @SuppressLint("MissingPermission") // checked by areNotificationsEnabled()
    private fun notify(task: TaskEntity, channel: String, title: String, text: String, markDone: Boolean): Boolean {
        if (!manager.areNotificationsEnabled()) return false
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(TextBrandColor.toArgb())
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Android 7 and older; channels decide on 8+
            .setAutoCancel(true)
            .setContentIntent(openReminderIntent(task.id))
        if (markDone) builder.addAction(R.drawable.ic_notification, "Mark done", markDoneIntent(task.id))
        return try {
            manager.notify(task.id, builder.build())
            true
        } catch (_: SecurityException) {
            false // permission revoked between the check and the call
        }
    }

    private fun reminderText(task: TaskEntity): String {
        val place = task.placeName
        return when {
            place != null && task.awayFromPlace -> "You've left $place"
            place != null -> "You're at $place"
            else -> "It's time"
        }
    }

    private fun openReminderIntent(taskId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_REMINDER_ID, taskId)
            // Reuse the running activity (onNewIntent) instead of stacking a second one
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        // The request code keeps each reminder's intent separate
        return PendingIntent.getActivity(context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun markDoneIntent(taskId: Int): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java)
            .setAction(NotificationActionReceiver.ACTION_MARK_DONE)
            .putExtra(EXTRA_REMINDER_ID, taskId)
        return PendingIntent.getBroadcast(context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        const val CHANNEL_REMINDERS = "reminders"
        const val CHANNEL_MISSED = "missed"
        const val EXTRA_REMINDER_ID = "com.example.remindme.REMINDER_ID"
    }
}
