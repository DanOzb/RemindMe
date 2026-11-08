package com.example.remindme.ui.profile

import com.example.remindme.data.location.LocationAccess
import com.example.remindme.ui.theme.Appearance

internal val sampleAccount = AccountUi(
    notificationsOn = true,
    locationAccess = LocationAccess.Always,
    exactAlarmsAllowed = true,
    defaultRadiusMeters = 200,
    appearance = Appearance.System,
    version = "1.2.0",
)
