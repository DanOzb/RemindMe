package com.example.remindme.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule

/** [active] highlights the summary, so set rules stand out from unused ones at a glance. */
@Composable
fun ExpandableCard(
    icon: ImageVector,
    label: String,
    summary: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = RemindTheme.colors
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")

    RemindCard(modifier = modifier) {
        CardHeader(
            icon = icon,
            label = label,
            modifier = Modifier
                .heightIn(min = 52.dp)
                .clickable(role = Role.Button, onClick = onToggle)
                .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.width(Spacing.md))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = if (active) colors.text else colors.textSecondary,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(Spacing.xs))
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(chevronRotation)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.lg),
                content = content
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ExpandableCardPreview() {
    PreviewSurface {
        ExpandableCard(
            icon = Icons.Outlined.LocationOn,
            label = "Location",
            summary = "Near Green Basket · 200 m",
            expanded = true,
            active = true,
            onToggle = {}
        ) {
            Text("Card controls go here", color = RemindTheme.colors.textSecondary)
        }
        ExpandableCard(
            icon = Icons.Outlined.Schedule,
            label = "Time",
            summary = "Any time",
            expanded = false,
            onToggle = {}
        ) {}
    }
}
