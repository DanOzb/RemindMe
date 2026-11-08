package com.example.remindme.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun PreviewSurface(content: @Composable ColumnScope.() -> Unit) {
    RemindMeTheme {
        Column(
            modifier = Modifier
                .background(RemindTheme.colors.bgPrimary)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            content = content
        )
    }
}
