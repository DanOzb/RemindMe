package com.example.remindme.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Stored by name, so don't rename these. */
enum class Appearance(val label: String, val description: String) {
    System("System", "Follows your phone's setting"),
    Light("Light", "Always light"),
    Dark("Dark", "Always dark"),
}

@Composable
fun Appearance.isDark(): Boolean = when (this) {
    Appearance.System -> isSystemInDarkTheme()
    Appearance.Light -> false
    Appearance.Dark -> true
}

/** Read synchronously at startup so the first frame already has the right theme. */
class AppearanceSettings private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _appearance = MutableStateFlow(
        prefs.getString(KEY, null)
            ?.let { name -> Appearance.entries.find { it.name == name } }
            ?: Appearance.System
    )
    val appearance: StateFlow<Appearance> = _appearance.asStateFlow()

    fun set(appearance: Appearance) {
        _appearance.value = appearance
        prefs.edit { putString(KEY, appearance.name) }
    }

    companion object {
        private const val KEY = "appearance"

        @Volatile
        private var instance: AppearanceSettings? = null

        fun getInstance(context: Context): AppearanceSettings =
            instance ?: synchronized(this) {
                instance ?: AppearanceSettings(context.applicationContext).also { instance = it }
            }
    }
}
