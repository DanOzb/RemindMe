package com.example.remindme

import android.app.Application
import com.example.remindme.notifications.ReminderNotifications

class RemindMeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Channels must exist before anything notifies, including receivers that run without the UI
        ReminderNotifications(this).createChannels()
    }
}
