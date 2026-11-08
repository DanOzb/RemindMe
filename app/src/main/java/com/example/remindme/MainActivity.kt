package com.example.remindme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.remindme.notifications.ReminderNotifications
import com.example.remindme.ui.navigation.AppNavHost
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.remindme.ui.theme.AppearanceSettings
import com.example.remindme.ui.theme.RemindMeTheme
import com.example.remindme.ui.theme.isDark

class MainActivity : ComponentActivity() {

    private val reminderToOpen = MutableStateFlow<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Only a fresh start: after rotation the screen is already showing it
        if (savedInstanceState == null) reminderToOpen.value = intent.reminderId()
        enableEdgeToEdge()
        val appearanceSettings = AppearanceSettings.getInstance(this)
        setContent {
            val appearance by appearanceSettings.appearance.collectAsStateWithLifecycle()
            val dark = appearance.isDark()

            // Status/navigation bar icons follow the app's theme, not the phone's
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { dark },
                )
                onDispose {}
            }

            RemindMeTheme(darkTheme = dark) {
                val openReminderId by reminderToOpen.collectAsStateWithLifecycle()
                AppNavHost(
                    openReminderId = openReminderId,
                    onReminderOpened = { reminderToOpen.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.reminderId()?.let { reminderToOpen.value = it }
    }
}

private fun Intent.reminderId(): Int? =
    getIntExtra(ReminderNotifications.EXTRA_REMINDER_ID, 0).takeIf { it > 0 }

// Same scrims enableEdgeToEdge() uses by default (only visible in 3-button navigation)
private val LightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
