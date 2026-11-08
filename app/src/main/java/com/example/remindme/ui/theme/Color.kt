package com.example.remindme.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val TextBrandColor = Color(0xFF3D694F)
val TextColor = Color(0xFF26392E)
val TextSecondaryColor = Color(0xFF778078)
val TextWarningColor = Color(0xFFB4442F)

val BGIcons = Color(0xFFE1EDBC)
val BGPrimary = Color(0xFFF7F6F1)
val BGReminderCard = Color(0xFFFFFFFF)
val BGGreenCards = Color(0xFFE8EEDF)
val BGSecondaryCards = Color(0xFFF0F1E9)

val BorderColor = Color(0xFFE2E5DA)
val Scrollbar = Color(0xFFA7AEA2)


val DarkTextBrandColor = Color(0xFF8FAE8C)
val DarkTextColor = Color(0xFFF7F6F1)
val DarkTextSecondaryColor = Color(0xFF778078)
val DarkTextWarningColor = Color(0xFFE3806B)

val DarkBGIcons = Color(0xFF2E3D34)
val DarkBGPrimary = Color(0xFF171F1A)
val DarkBGReminderCard = Color(0xFF1F2824)
val DarkBGGreenCards = Color(0xFF243029)
val DarkBGSecondaryCards = Color(0xFF1B2320)

val DarkBorderColor = Color(0xFF3A463F)
val DarkScrollbar = Color(0xFF8FAE8C)

@Immutable
data class AppColors(
    val textBrand: Color,
    val text: Color,
    val textSecondary: Color,
    val textWarning: Color,
    val textOnBrand: Color,
    val bgIcons: Color,
    val bgPrimary: Color,
    val bgReminderCard: Color,
    val bgGreenCards: Color,
    val bgSecondaryCards: Color,
    val border: Color,
    val scrollbar: Color,
    val chipSelected: Color,
    val onChipSelected: Color,
)

val LightAppColors = AppColors(
    textBrand = TextBrandColor,
    text = TextColor,
    textSecondary = TextSecondaryColor,
    textWarning = TextWarningColor,
    textOnBrand = Color.White,
    bgIcons = BGIcons,
    bgPrimary = BGPrimary,
    bgReminderCard = BGReminderCard,
    bgGreenCards = BGGreenCards,
    bgSecondaryCards = BGSecondaryCards,
    border = BorderColor,
    scrollbar = Scrollbar,
    chipSelected = BGGreenCards,
    onChipSelected = TextBrandColor,
)

val DarkAppColors = AppColors(
    textBrand = DarkTextBrandColor,
    text = DarkTextColor,
    textSecondary = DarkTextSecondaryColor,
    textWarning = DarkTextWarningColor,
    textOnBrand = DarkBGPrimary,
    bgIcons = DarkBGIcons,
    bgPrimary = DarkBGPrimary,
    bgReminderCard = DarkBGReminderCard,
    bgGreenCards = DarkBGGreenCards,
    bgSecondaryCards = DarkBGSecondaryCards,
    border = DarkBorderColor,
    scrollbar = DarkScrollbar,
    chipSelected = DarkTextBrandColor,
    onChipSelected = DarkBGPrimary,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }