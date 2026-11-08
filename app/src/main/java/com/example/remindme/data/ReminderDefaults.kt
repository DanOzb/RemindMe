package com.example.remindme.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

val RadiusOptions = listOf(100, 200, 500)
const val DEFAULT_RADIUS_METERS = 200

class ReminderDefaults private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _radiusMeters = MutableStateFlow(
        prefs.getInt(KEY_RADIUS, DEFAULT_RADIUS_METERS).takeIf { it in RadiusOptions } ?: DEFAULT_RADIUS_METERS
    )
    val radiusMeters: StateFlow<Int> = _radiusMeters.asStateFlow()

    fun setRadius(meters: Int) {
        _radiusMeters.value = meters
        prefs.edit { putInt(KEY_RADIUS, meters) }
    }

    companion object {
        private const val KEY_RADIUS = "default_radius_meters"

        @Volatile
        private var instance: ReminderDefaults? = null

        fun getInstance(context: Context): ReminderDefaults =
            instance ?: synchronized(this) {
                instance ?: ReminderDefaults(context.applicationContext).also { instance = it }
            }
    }
}
