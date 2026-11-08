package com.example.remindme.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.remindme.ui.main.MainScreen
import com.example.remindme.ui.reminder.ReminderScreen
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.RemindMeTheme

@Composable
fun AppNavHost(openReminderId: Int? = null, onReminderOpened: () -> Unit = {}) {
    val rootNavController = rememberNavController()

    LaunchedEffect(openReminderId) {
        if (openReminderId != null) {
            rootNavController.navigate(ReminderRoute(openReminderId)) {
                // Replace a reminder that's already open, so Back goes straight to the main pager
                popUpTo<MainRoute>()
            }
            onReminderOpened()
        }
    }

    NavHost(
        navController = rootNavController,
        startDestination = MainRoute
    ) {
        composable<MainRoute>(
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None }
        ) {
            MainScreen(
                onNewReminder = { rootNavController.navigate(ReminderRoute()) },
                onEditReminder = { id -> rootNavController.navigate(ReminderRoute(id)) }
            )
        }

        composable<ReminderRoute>(
            enterTransition = { slideInVertically(initialOffsetY = { it }) },
            popExitTransition = { slideOutVertically(targetOffsetY = { it }) }
        ) { entry ->
            ReminderScreen(
                reminderId = entry.toRoute<ReminderRoute>().id,
                onClose = {
                    // Guard against double taps popping the main screen too
                    if (rootNavController.previousBackStackEntry != null) {
                        rootNavController.popBackStack()
                    }
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AppNavHostPreview() {
    RemindMeTheme {
        AppNavHost()
    }
}
