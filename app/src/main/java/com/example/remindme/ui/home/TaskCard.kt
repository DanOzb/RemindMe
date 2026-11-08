package com.example.remindme.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.data.TaskEntity
import com.example.remindme.data.TaskWithDistance
import com.example.remindme.data.isActiveNow
import com.example.remindme.data.todayEpochDay
import com.example.remindme.ui.components.RemindCard
import com.example.remindme.ui.components.RemindSwitch
import com.example.remindme.ui.theme.RemindMeTheme
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing

@Composable
fun TaskCard(
    item: TaskWithDistance,
    onToggle: (TaskEntity) -> Unit,
    onClick: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier,
    locationTracked: Boolean = true,
) {
    val colors = RemindTheme.colors
    val task = item.task
    val active = !task.completed
    // Can't fire matters more than "you're close", so it wins when both apply
    val notTracked = active && task.latitude != null && !locationTracked
    val nearby = !notTracked && item.isActiveNow
    val status = notTracked || nearby
    val info = item.whereAndWhen()

    RemindCard(
        modifier = modifier,
        containerColor = if (active) colors.bgReminderCard else colors.bgSecondaryCards,
        onClick = { onClick(task) }
    ) {
        Row(
            modifier = Modifier.padding(
                start = Spacing.lg,
                end = Spacing.md,
                top = Spacing.md,
                bottom = Spacing.md
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (active) colors.text else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (status || info.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (status) {
                            if (notTracked) NotTrackedIndicator() else NearbyIndicator()
                            if (info.isNotEmpty()) {
                                Text(
                                    text = " · ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        Text(
                            text = info,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.width(Spacing.md))

            RemindSwitch(
                checked = active,
                onCheckedChange = { onToggle(task) },
                modifier = Modifier.semantics {
                    contentDescription = if (active) "${task.title}, active" else "${task.title}, done"
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TaskCardPreview() {
    RemindMeTheme {
        Column(
            modifier = Modifier
                .background(RemindTheme.colors.bgPrimary)
                .padding(Spacing.lg)
        ) {
            TaskCard(
                TaskWithDistance(TaskEntity(1, "Buy apples", placeName = "Green Basket", radiusMeters = 200), 150.0),
                onToggle = {},
                onClick = {}
            )
            Spacer(Modifier.size(Spacing.sm))
            TaskCard(
                TaskWithDistance(TaskEntity(2, "Call Mum", dueDate = todayEpochDay(), dueTimeMinutes = 18 * 60), null),
                onToggle = {},
                onClick = {}
            )
            Spacer(Modifier.size(Spacing.sm))
            TaskCard(
                TaskWithDistance(TaskEntity(4, "Water the plants", placeName = "Home", latitude = 59.33, longitude = 18.06, radiusMeters = 100), null),
                onToggle = {},
                onClick = {},
                locationTracked = false
            )
            Spacer(Modifier.size(Spacing.sm))
            TaskCard(
                TaskWithDistance(TaskEntity(3, "Return library books", completed = true, placeName = "Maple Library"), null),
                onToggle = {},
                onClick = {}
            )
        }
    }
}
