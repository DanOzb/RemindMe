package com.example.remindme.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.remindme.data.location.LocationAccess
import com.example.remindme.data.location.PermissionPrefs
import com.example.remindme.data.location.PermissionStatus
import com.example.remindme.data.location.isGranted
import com.example.remindme.data.location.locationAccess
import com.example.remindme.data.location.notificationsAllowed
import com.example.remindme.data.location.openAppSettings
import com.example.remindme.data.location.openExactAlarmSettings
import com.example.remindme.data.location.openNotificationSettings
import com.example.remindme.data.location.permissionStatus
import com.example.remindme.triggers.canScheduleExactAlarms
import com.example.remindme.ui.components.DialogConfirmButton
import com.example.remindme.ui.components.DialogDismissButton
import com.example.remindme.ui.theme.RemindMeTheme
import com.example.remindme.ui.theme.RemindTheme

private const val FINE = Manifest.permission.ACCESS_FINE_LOCATION
private const val COARSE = Manifest.permission.ACCESS_COARSE_LOCATION
private const val BACKGROUND = Manifest.permission.ACCESS_BACKGROUND_LOCATION
private const val NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS
/** Not a permission: records that the "open settings" explanation was shown. */
private const val SETTINGS_EXPLAINED = "location_settings_explained"
private const val EXACT_ALARMS_EXPLAINED = "exact_alarms_explained"

/** Current permissions, re-read whenever the screen resumes (e.g. back from a dialog or system settings). */
@Composable
fun rememberPermissionStatus(): State<PermissionStatus> {
    val context = LocalContext.current
    val status = remember { mutableStateOf(context.permissionStatus()) }
    LifecycleResumeEffect(Unit) {
        status.value = context.permissionStatus()
        onPauseOrDispose {}
    }
    return status
}

@Composable
fun rememberPermissionPrefs(): PermissionPrefs {
    val context = LocalContext.current
    return remember { PermissionPrefs(context) }
}

/**
 * Asks for notifications. Pass `userTapped = true` from a settings row: then it also opens system
 * settings when Android won't show the dialog any more, or when turning notifications off.
 * Without a tap it only shows the system dialog, and only if it still can.
 */
@Composable
fun rememberNotificationRequest(onFinished: (Boolean) -> Unit = {}): (userTapped: Boolean) -> Unit {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val prefs = rememberPermissionPrefs()
    val launcher = rememberLauncherForActivityResult(RequestPermission()) { onFinished(context.notificationsAllowed()) }

    return { userTapped ->
        val canAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !context.isGranted(NOTIFICATIONS) && !isBlocked(activity, prefs, NOTIFICATIONS)
        when {
            canAsk -> {
                prefs.markAsked(NOTIFICATIONS)
                launcher.launch(NOTIFICATIONS)
            }
            // Already on (so this is "turn off"), turned off in settings, or denied for good
            userTapped -> context.openNotificationSettings()
            else -> onFinished(context.notificationsAllowed())
        }
    }
}

/**
 * Gets location access to "all the time", which geofences need.
 *
 * - Android 11+: the system dialog never offers "all the time", so skip it. Explain what to pick,
 *   then open the app's settings page, where one choice grants it.
 * - Android 10: one dialog that includes "Allow all the time".
 * - Older: the normal dialog; there's no separate background permission.
 *
 * Pass `userTapped = true` from a settings row. Without a tap (e.g. picking a place) it explains only
 * once and never opens settings by itself.
 */
@Composable
fun rememberLocationRequest(onFinished: (LocationAccess) -> Unit = {}): (userTapped: Boolean) -> Unit {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val prefs = rememberPermissionPrefs()
    var explain by remember { mutableStateOf(false) }
    var waitingForSettings by remember { mutableStateOf(false) }

    // Back from the settings page: report what was picked. Keyed on Unit so it only runs on a real
    // resume; keying on the flag would re-run it right away, before settings has even opened.
    LifecycleResumeEffect(Unit) {
        if (waitingForSettings) {
            waitingForSettings = false
            onFinished(context.locationAccess())
        }
        onPauseOrDispose {}
    }

    val launcher = rememberLauncherForActivityResult(RequestMultiplePermissions()) {
        onFinished(context.locationAccess())
    }

    if (explain) {
        LocationSettingsDialog(
            optionLabel = backgroundOptionLabel(context),
            onOpenSettings = {
                explain = false
                waitingForSettings = true
                context.openAppSettings()
            },
            onDismiss = {
                explain = false
                onFinished(context.locationAccess())
            }
        )
    }

    return { tapped ->
        val access = context.locationAccess()
        when {
            // Nothing left to ask; from a settings row, let them change it
            access == LocationAccess.Always ->
                if (tapped) context.openAppSettings() else onFinished(access)

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                if (tapped || !prefs.asked(SETTINGS_EXPLAINED)) {
                    prefs.markAsked(SETTINGS_EXPLAINED)
                    explain = true
                } else {
                    onFinished(access)
                }

            isBlocked(activity, prefs, FINE) ->
                if (tapped) context.openAppSettings() else onFinished(access)

            else -> {
                prefs.markAsked(FINE)
                val permissions = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) arrayOf(FINE, COARSE, BACKGROUND)
                else arrayOf(FINE, COARSE)
                launcher.launch(permissions)
            }
        }
    }
}

/** The returned function takes what to do afterwards; it runs right away when there's nothing to ask. */
@Composable
fun rememberExactAlarmRequest(): (then: () -> Unit) -> Unit {
    val context = LocalContext.current
    val prefs = rememberPermissionPrefs()
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }

    pending?.let { then ->
        ExactAlarmDialog(
            onOpenSettings = {
                pending = null
                context.openExactAlarmSettings()
                then()
            },
            onDismiss = {
                pending = null
                then()
            }
        )
    }

    return { then ->
        if (context.canScheduleExactAlarms() || prefs.asked(EXACT_ALARMS_EXPLAINED)) {
            then()
        } else {
            prefs.markAsked(EXACT_ALARMS_EXPLAINED)
            pending = then
        }
    }
}

@Composable
private fun ExactAlarmDialog(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    val colors = RemindTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remind me on time") },
        text = {
            Text(
                "To remind you at exactly the time you picked, RemindMe needs the \"Alarms & reminders\" " +
                    "permission. Without it, Android may deliver reminders a few minutes late.\n\n" +
                    "On the next screen, turn on \"Allow setting alarms and reminders\"."
            )
        },
        confirmButton = { DialogConfirmButton("Open settings", onClick = onOpenSettings) },
        dismissButton = { DialogDismissButton(onClick = onDismiss, text = "Not now") },
        containerColor = colors.bgReminderCard,
        titleContentColor = colors.text,
        textContentColor = colors.textSecondary
    )
}

/** Asked before, and Android no longer offers an explanation: the dialog won't show again. */
private fun isBlocked(activity: Activity?, prefs: PermissionPrefs, permission: String): Boolean =
    prefs.asked(permission) && activity != null &&
        !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

private fun backgroundOptionLabel(context: Context): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) context.packageManager.backgroundPermissionOptionLabel.toString()
    else "Allow all the time"

@Composable
private fun LocationSettingsDialog(optionLabel: String, onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    val colors = RemindTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remind me when I arrive") },
        text = {
            Text(
                "To notice when you reach or leave a place, even while the app is closed, " +
                    "RemindMe needs location access all the time. It's only used to check your reminders' places.\n\n" +
                    "In settings, open Permissions → Location and choose \"$optionLabel\". " +
                    "Keep \"Use precise location\" on."
            )
        },
        confirmButton = { DialogConfirmButton("Open settings", onClick = onOpenSettings) },
        dismissButton = { DialogDismissButton(onClick = onDismiss, text = "Not now") },
        containerColor = colors.bgReminderCard,
        titleContentColor = colors.text,
        textContentColor = colors.textSecondary
    )
}

@PreviewLightDark
@Composable
private fun LocationSettingsDialogPreview() {
    RemindMeTheme {
        LocationSettingsDialog(optionLabel = "Allow all the time", onOpenSettings = {}, onDismiss = {})
    }
}
