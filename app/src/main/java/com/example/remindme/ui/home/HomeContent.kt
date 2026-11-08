package com.example.remindme.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.remindme.data.TaskEntity
import com.example.remindme.data.TaskWithDistance
import com.example.remindme.data.sampleActive
import com.example.remindme.data.sampleCompleted
import com.example.remindme.ui.components.CollapsingHeaderLayout
import com.example.remindme.ui.theme.RemindMeTheme
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing

@Composable
fun HomeContent(
    active: List<TaskWithDistance>,
    completed: List<TaskEntity>,
    onToggle: (TaskEntity) -> Unit,
    onTaskClick: (TaskEntity) -> Unit,
    onNewReminder: () -> Unit,
    modifier: Modifier = Modifier,
    /** False when location access isn't "all the time": place reminders then show "Not tracked". */
    locationTracked: Boolean = true,
    onOpenSettings: () -> Unit = {},
    onTestNotification: (() -> Unit)? = null,
) {
    // The two lists update separately, so a toggled task can briefly be in both; keep one copy
    val items = (active + completed.map { TaskWithDistance(it, null) }).distinctBy { it.task.id }

    // Order only resets when a new reminder appears, so toggling (or moving) doesn't make cards jump.
    // Also once when the first location fix arrives, since it lands just after the tasks do.
    val frozen = remember { FrozenOrder() }
    val ids = items.map { it.task.id }
    val hasDistances = active.any { it.distanceMeters != null }
    if (ids.any { it !in frozen.idSet } || hasDistances != frozen.hasDistances) frozen.set(ids, hasDistances)
    val byId = items.associateBy { it.task.id }
    val ordered = frozen.ids.mapNotNull { byId[it] }

    val nextUp = active.firstOrNull()

    CollapsingHeaderLayout(
        collapsedTitle = "Reminders",
        modifier = modifier,
        expandedHeight = 140.dp,
        expandedContent = { NextUpHeader(nextUp) },
        actions = {
            AddReminderButton(onClick = onNewReminder)
            HomeMenuButton(onOpenSettings = onOpenSettings, onTestNotification = onTestNotification)
        }
    ) {
        TaskList(
            tasks = ordered,
            onToggle = onToggle,
            onClick = onTaskClick,
            modifier = Modifier.padding(horizontal = Spacing.screenHorizontal),
            locationTracked = locationTracked
        )
    }
}

private class FrozenOrder {
    var ids: List<Int> = emptyList()
        private set
    var idSet: Set<Int> = emptySet()
        private set
    var hasDistances = false
        private set

    fun set(newIds: List<Int>, distances: Boolean) {
        ids = newIds
        idSet = newIds.toSet()
        hasDistances = distances
    }
}

@PreviewLightDark
@Composable
private fun HomeContentPreview() {
    RemindMeTheme {
        HomeContent(
            active = sampleActive,
            completed = sampleCompleted,
            onToggle = {},
            onTaskClick = {},
            onNewReminder = {},
            modifier = Modifier.background(RemindTheme.colors.bgPrimary)
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeContentEmptyPreview() {
    RemindMeTheme {
        HomeContent(
            active = emptyList(),
            completed = emptyList(),
            onToggle = {},
            onTaskClick = {},
            onNewReminder = {},
            modifier = Modifier.background(RemindTheme.colors.bgPrimary)
        )
    }
}
