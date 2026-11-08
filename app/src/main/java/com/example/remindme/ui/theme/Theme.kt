package com.example.remindme.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun AppColors.toMaterial(dark: Boolean) =
    (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = textBrand,
        onPrimary = textOnBrand,
        primaryContainer = bgGreenCards,
        onPrimaryContainer = text,
        background = bgPrimary,
        onBackground = text,
        surface = bgReminderCard,
        onSurface = text,
        surfaceVariant = bgSecondaryCards,
        onSurfaceVariant = textSecondary,
        outline = border,
        outlineVariant = border,
    )

@Composable
fun RemindMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = appColors.toMaterial(darkTheme),
            typography = Typography,
            content = content
        )
    }
}

object RemindTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}