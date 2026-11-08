package com.example.remindme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remindme.ui.theme.RemindShapes
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

@Composable
fun TagPill(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null
) {
    val colors = RemindTheme.colors

    Row(
        modifier = modifier
            .then(if (onClick != null) Modifier.minimumInteractiveComponentSize() else Modifier)
            .clip(RemindShapes.pill)
            .background(colors.chipSelected)
            .then(
                if (onClick != null) Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.6.sp),
            color = colors.onChipSelected
        )
        if (onClick != null) {
            Spacer(Modifier.width(3.dp))
            Icon(
                Icons.Filled.SwapHoriz,
                contentDescription = null,
                tint = colors.onChipSelected,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TagPillPreview() {
    PreviewSurface {
        TagPill("Both are true")
        TagPill("Either is true", onClick = {})
    }
}
