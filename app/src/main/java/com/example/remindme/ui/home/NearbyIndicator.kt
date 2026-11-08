package com.example.remindme.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

@Composable
internal fun NearbyIndicator() = StatusIndicator("Nearby", RemindTheme.colors.textBrand)

@Composable
internal fun NotTrackedIndicator() = StatusIndicator("Not tracked", RemindTheme.colors.textWarning)

@Composable
private fun StatusIndicator(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(
            Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@PreviewLightDark
@Composable
private fun NearbyIndicatorPreview() {
    PreviewSurface {
        NearbyIndicator()
        NotTrackedIndicator()
    }
}
