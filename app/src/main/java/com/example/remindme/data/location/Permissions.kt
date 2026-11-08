package com.example.remindme.data.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.remindme.triggers.canScheduleExactAlarms

enum class LocationAccess(val label: String) {
    Off("Not allowed"),
    Approximate("Approximate location only"),
    WhileUsing("Only while using the app"),
    Always("Allowed all the time"),
}

data class PermissionStatus(val location: LocationAccess, val notifications: Boolean, val exactAlarms: Boolean)

fun Context.permissionStatus() = PermissionStatus(locationAccess(), notificationsAllowed(), canScheduleExactAlarms())

fun Context.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.locationAccess(): LocationAccess {
    val fine = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
    // Before Android 10 there's no separate background permission: foreground access covers it
    val background = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    return when {
        !fine && !coarse -> LocationAccess.Off
        !fine -> LocationAccess.Approximate
        !background -> LocationAccess.WhileUsing
        else -> LocationAccess.Always
    }
}

/** Covers both the Android 13+ permission and the user turning notifications off in settings. */
fun Context.notificationsAllowed(): Boolean = NotificationManagerCompat.from(this).areNotificationsEnabled()

fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun Context.openNotificationSettings() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return openAppSettings()
    startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun Context.openExactAlarmSettings() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return openAppSettings()
    startActivity(
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

class PermissionPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("permissions", Context.MODE_PRIVATE)

    fun asked(permission: String): Boolean = prefs.getBoolean(permission, false)
    fun markAsked(permission: String) = prefs.edit { putBoolean(permission, true) }
}
