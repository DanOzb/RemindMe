package com.example.remindme.ui.profile

import com.example.remindme.data.location.LocationAccess
import com.example.remindme.ui.theme.Appearance

data class AccountUi(
    val notificationsOn: Boolean,
    val locationAccess: LocationAccess,
    val exactAlarmsAllowed: Boolean,
    val defaultRadiusMeters: Int,
    val appearance: Appearance,
    val version: String,
)
