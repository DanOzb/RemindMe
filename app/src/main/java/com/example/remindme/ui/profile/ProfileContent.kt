package com.example.remindme.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.components.CollapsingHeaderLayout
import com.example.remindme.ui.components.LargeHeaderTitle
import com.example.remindme.ui.components.RemindCard
import com.example.remindme.ui.components.RemindSwitch
import com.example.remindme.ui.components.SectionLabel
import com.example.remindme.ui.components.SettingsDivider
import com.example.remindme.ui.components.SettingsRow
import com.example.remindme.data.RadiusOptions
import com.example.remindme.ui.theme.Appearance
import com.example.remindme.ui.theme.RemindMeTheme
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing

@Composable
fun ProfileContent(
    account: AccountUi,
    onNotificationsChange: (Boolean) -> Unit,
    onAppearanceChange: (Appearance) -> Unit,
    onDefaultRadiusChange: (Int) -> Unit,
    onAction: (AccountAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RemindTheme.colors
    var showAppearance by rememberSaveable { mutableStateOf(false) }
    var showRadius by rememberSaveable { mutableStateOf(false) }

    CollapsingHeaderLayout(
        collapsedTitle = "Account & settings",
        modifier = modifier,
        expandedHeight = 96.dp,
        expandedContent = { LargeHeaderTitle("Account & settings") }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
        ) {
            SectionLabel("Your reminders")
            Spacer(Modifier.height(Spacing.sm))
            RemindCard {
                SettingsRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Notifications",
                    onClick = { onNotificationsChange(!account.notificationsOn) },
                    trailing = {
                        RemindSwitch(
                            checked = account.notificationsOn,
                            onCheckedChange = onNotificationsChange
                        )
                    }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Outlined.LocationOn,
                    title = "Location access",
                    subtitle = account.locationAccess.label,
                    onClick = { onAction(AccountAction.LocationAccess) }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Outlined.Alarm,
                    title = "Alarms & reminders",
                    subtitle = if (account.exactAlarmsAllowed) "Allowed" else "Not allowed",
                    onClick = { onAction(AccountAction.ExactAlarms) }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Outlined.TrackChanges,
                    title = "Default radius",
                    subtitle = "How close is nearby?",
                    value = "${account.defaultRadiusMeters} m",
                    onClick = { showRadius = true }
                )
            }
            Spacer(Modifier.height(Spacing.xl))

            SectionLabel("Make it yours")
            Spacer(Modifier.height(Spacing.sm))
            RemindCard {
                SettingsRow(
                    icon = Icons.Outlined.Palette,
                    title = "Appearance",
                    subtitle = account.appearance.description,
                    value = account.appearance.label,
                    onClick = { showAppearance = true }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Outlined.Shield,
                    title = "Privacy & data",
                    subtitle = "Your places stay yours",
                    onClick = { onAction(AccountAction.Privacy) }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    title = "Help & support",
                    subtitle = "We're here if you need us",
                    onClick = { onAction(AccountAction.Help) }
                )
            }
            Spacer(Modifier.height(Spacing.lg))

            Text(
                text = "remind · v${account.version}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(Spacing.lg))
        }
    }

    if (showAppearance) {
        ChoiceDialog(
            title = "Appearance",
            options = Appearance.entries,
            selected = account.appearance,
            label = { it.label },
            description = { it.description },
            onSelect = {
                showAppearance = false
                onAppearanceChange(it)
            },
            onDismiss = { showAppearance = false }
        )
    }

    if (showRadius) {
        ChoiceDialog(
            title = "Default radius",
            options = RadiusOptions,
            selected = account.defaultRadiusMeters,
            label = { "$it m" },
            description = ::radiusDescription,
            onSelect = {
                showRadius = false
                onDefaultRadiusChange(it)
            },
            onDismiss = { showRadius = false }
        )
    }
}

private fun radiusDescription(meters: Int): String = when {
    meters <= 100 -> "One shop or building"
    meters <= 200 -> "A block or two"
    else -> "The neighbourhood"
}

@PreviewLightDark
@Composable
private fun ProfileContentPreview() {
    RemindMeTheme {
        ProfileContent(
            account = sampleAccount,
            onNotificationsChange = {},
            onAppearanceChange = {},
            onDefaultRadiusChange = {},
            onAction = {},
            modifier = Modifier.background(RemindTheme.colors.bgPrimary)
        )
    }
}
