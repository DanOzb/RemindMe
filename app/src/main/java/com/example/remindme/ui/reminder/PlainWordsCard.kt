package com.example.remindme.ui.reminder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.remindme.ui.components.CardHeader
import com.example.remindme.ui.components.RemindCard
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

@Composable
internal fun PlainWordsCard(form: ReminderForm) {
    val colors = RemindTheme.colors

    RemindCard(containerColor = colors.bgGreenCards, border = false) {
        Column(Modifier.padding(Spacing.lg)) {
            CardHeader(Icons.Outlined.AutoAwesome, "Your reminder, in plain words")
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = plainWords(form),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.text
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PlainWordsCardPreview() {
    PreviewSurface {
        PlainWordsCard(
            ReminderForm(
                title = "Buy apples",
                place = "Green Basket",
                timeMode = TimeMode.Between,
                hour = 9,
                endHour = 17
            )
        )
    }
}
