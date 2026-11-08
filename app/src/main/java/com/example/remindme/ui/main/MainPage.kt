package com.example.remindme.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainPage(val label: String, val icon: ImageVector) {
    Home("Reminders", Icons.Default.Layers),
    Account("Account", Icons.Default.Person),
}
