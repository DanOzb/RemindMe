package com.example.remindme.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.remindme.data.ReminderDefaults
import com.example.remindme.data.location.openExactAlarmSettings
import com.example.remindme.ui.permissions.rememberLocationRequest
import com.example.remindme.ui.tasksViewModel
import com.example.remindme.ui.permissions.rememberNotificationRequest
import com.example.remindme.ui.permissions.rememberPermissionStatus
import com.example.remindme.ui.theme.AppearanceSettings

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onAction: (AccountAction) -> Unit = {}
) {
    val context = LocalContext.current
    val appearanceSettings = AppearanceSettings.getInstance(context)
    val appearance by appearanceSettings.appearance.collectAsStateWithLifecycle()
    val radius by ReminderDefaults.getInstance(context).radiusMeters.collectAsStateWithLifecycle()
    val viewModel = tasksViewModel()
    val permissions by rememberPermissionStatus()
    val requestNotifications = rememberNotificationRequest()
    val requestLocation = rememberLocationRequest()
    // The rest is still sample data; swap for a ViewModel later.
    val account = remember { sampleAccount }

    ProfileContent(
        account = account.copy(
            appearance = appearance,
            notificationsOn = permissions.notifications,
            locationAccess = permissions.location,
            exactAlarmsAllowed = permissions.exactAlarms,
            defaultRadiusMeters = radius,
        ),
        // Both directions go through Android: asking, or settings to turn them off
        onNotificationsChange = { requestNotifications(true) },
        onAppearanceChange = appearanceSettings::set,
        onDefaultRadiusChange = viewModel::setDefaultRadius,
        onAction = { action ->
            when (action) {
                AccountAction.LocationAccess -> requestLocation(true)
                AccountAction.ExactAlarms -> context.openExactAlarmSettings()
                else -> onAction(action)
            }
        },
        modifier = modifier
    )
}
