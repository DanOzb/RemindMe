package com.example.remindme.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object MainRoute

/** Create/Edit reminder. Without an id it creates, with an id it edits. */
@Serializable
data class ReminderRoute(val id: Int? = null)
