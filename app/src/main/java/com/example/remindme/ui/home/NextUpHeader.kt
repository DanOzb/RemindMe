package com.example.remindme.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.remindme.data.TaskEntity
import com.example.remindme.data.TaskWithDistance
import com.example.remindme.data.isInRange
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface
import androidx.compose.ui.unit.dp

/** Big "what's next" text. In range → time-based → anything else → all clear. */
@Composable
internal fun NextUpHeader(item: TaskWithDistance?) {
    val colors = RemindTheme.colors
    val task = item?.task

    val eyebrow = when {
        task == null -> "All clear"
        item.isInRange -> "You're nearby"
        task.dueDate != null || task.dueTimeMinutes != null -> "Next up"
        else -> "Waiting for the right place"
    }
    val detail = when (task) {
        null -> "Tap + to add a reminder"
        else -> item.whereAndWhen()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = eyebrow.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textBrand
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = task?.title ?: "Nothing coming up",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (detail.isNotEmpty()) {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NextUpHeaderPreview() {
    PreviewSurface {
        Column(Modifier.height(140.dp)) {
            NextUpHeader(TaskWithDistance(TaskEntity(1, "Buy apples", placeName = "Green Basket", radiusMeters = 200), 150.0))
        }
        Column(Modifier.height(140.dp)) {
            NextUpHeader(null)
        }
    }
}
