package com.example.remindme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.RemindShapes
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import com.example.remindme.ui.theme.Spacing

@Composable
fun RemindCard(
    modifier: Modifier = Modifier,
    containerColor: Color = RemindTheme.colors.bgReminderCard,
    border: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RemindShapes.card
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .then(if (border) Modifier.border(1.dp, RemindTheme.colors.border, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content
    )
}

@PreviewLightDark
@Composable
private fun RemindCardPreview() {
    PreviewSurface {
        RemindCard {
            Text("Default card", color = RemindTheme.colors.text, modifier = Modifier.padding(Spacing.lg))
        }
        RemindCard(containerColor = RemindTheme.colors.bgGreenCards, border = false) {
            Text("Green card, no border", color = RemindTheme.colors.text, modifier = Modifier.padding(Spacing.lg))
        }
    }
}
