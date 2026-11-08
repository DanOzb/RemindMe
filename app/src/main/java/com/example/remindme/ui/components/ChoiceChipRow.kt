package com.example.remindme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.RemindShapes
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

@Composable
fun <T> ChoiceChipRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    val colors = RemindTheme.colors
    val shape = RemindShapes.field

    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val fg = if (isSelected) colors.onChipSelected else colors.textSecondary

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(shape)
                    .background(if (isSelected) colors.chipSelected else colors.bgSecondaryCards)
                    .then(
                        if (isSelected) Modifier.border(1.dp, colors.onChipSelected.copy(alpha = 0.3f), shape)
                        else Modifier
                    )
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(option) }
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = fg,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(Spacing.xs))
                }
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelMedium,
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ChoiceChipRowPreview() {
    PreviewSurface {
        ChoiceChipRow(
            options = listOf("Any time", "At", "Between"),
            selected = "At",
            onSelect = {},
            label = { it }
        )
    }
}
