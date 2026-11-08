package com.example.remindme.ui.reminder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.RemindShapes
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

@Composable
internal fun PickerRow(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val colors = RemindTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RemindShapes.field)
            .clickable(onClickLabel = "Change", onClick = onClick)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.width(44.dp)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = colors.text,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Change",
            style = MaterialTheme.typography.labelMedium,
            color = colors.textBrand
        )
    }
}

@Composable
internal fun CardHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = RemindTheme.colors.textSecondary,
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun PickerRowPreview() {
    PreviewSurface {
        PickerRow(value = "09:00", onClick = {})
        PickerRow(label = "From", value = "09:00", onClick = {})
        PickerRow(label = "To", value = "17:00", onClick = {})
    }
}

@PreviewLightDark
@Composable
private fun CardHintPreview() {
    PreviewSurface {
        CardHint("Runs overnight, ending the next day")
    }
}
