package com.example.remindme.ui.home

import android.Manifest
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.remindme.data.location.openAppSettings
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.remindme.notifications.ReminderNotifications
import com.example.remindme.ui.TasksViewModel
import com.example.remindme.ui.permissions.rememberNotificationRequest
import com.example.remindme.ui.permissions.rememberPermissionPrefs
import com.example.remindme.ui.permissions.rememberPermissionStatus
import com.example.remindme.data.location.LocationAccess
import com.example.remindme.ui.tasksViewModel
import com.example.remindme.triggers.canReactivate

@Composable
fun HomeScreen(
    onNewReminder: () -> Unit,
    onEditReminder: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = tasksViewModel(),
) {
    val active by viewModel.activeTasks.collectAsStateWithLifecycle()
    val completed by viewModel.completedTasks.collectAsStateWithLifecycle()
    val permissions by rememberPermissionStatus()
    val context = LocalContext.current

    // Each time Home is shown: a fresh location for "Nearby" and sorting, and triggers rebuilt in case
    // permissions changed in settings (e.g. location "all the time" now granted)
    LifecycleResumeEffect(Unit) {
        viewModel.refreshLocation()
        viewModel.syncTriggers()
        onPauseOrDispose {}
    }

    // Ask for notifications once, when there's first a reminder to be notified about
    val prefs = rememberPermissionPrefs()
    val askNotifications = rememberNotificationRequest()
    val notifications = remember { ReminderNotifications(context) }
    val debuggable = remember { (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 }
    val hasReminders = active.isNotEmpty()
    LaunchedEffect(hasReminders) {
        if (hasReminders && !prefs.asked(Manifest.permission.POST_NOTIFICATIONS)) askNotifications(false)
    }

    HomeContent(
        active = active,
        completed = completed,
        onToggle = { task ->
            // A one-off date in the past would expire straight away: let them pick a new one instead
            if (task.completed && !task.canReactivate(System.currentTimeMillis())) onEditReminder(task.id)
            else viewModel.setCompleted(task.id, !task.completed)
        },
        onTaskClick = { onEditReminder(it.id) },
        onNewReminder = onNewReminder,
        modifier = modifier,
        locationTracked = permissions.location == LocationAccess.Always,
        onOpenSettings = { context.openAppSettings() },
        onTestNotification = active.firstOrNull()?.takeIf { debuggable }?.let { next ->
            { if (!notifications.showReminder(next.task)) askNotifications(true) }
        }
    )
}
