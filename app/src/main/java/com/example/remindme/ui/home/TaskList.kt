package com.example.remindme.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.remindme.data.TaskEntity
import com.example.remindme.data.TaskWithDistance
import com.example.remindme.ui.components.verticalLazyScrollbar
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.RemindMeTheme
import androidx.compose.foundation.background
import com.example.remindme.data.sampleActive
import com.example.remindme.data.sampleCompleted

@Composable
fun TaskList(
    tasks: List<TaskWithDistance>,
    onToggle: (TaskEntity) -> Unit,
    onClick: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier,
    locationTracked: Boolean = true,
) {
    val listState = rememberLazyListState()
    val colors = RemindTheme.colors

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .verticalLazyScrollbar(
                state = listState,
                thumbHeight = 48.dp,
                trackPadding = Spacing.sm,
                scrollBarTrackColor = colors.border,
                scrollBarColor = colors.scrollbar,
                endPadding = 0.dp
            ),
        contentPadding = PaddingValues(top = Spacing.sm, end = Spacing.md, bottom = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        if (tasks.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = "Nothing here yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = Spacing.lg)
                )
            }
        }

        items(tasks, key = { it.task.id }) { task ->
            TaskCard(task, onToggle, onClick, Modifier.animateItem(), locationTracked)
        }
    }
}

@PreviewLightDark
@Composable
private fun TaskListPreview() {
    RemindMeTheme {
        TaskList(
            tasks = sampleActive + sampleCompleted.map { TaskWithDistance(it, null) },
            onToggle = {},
            onClick = {},
            modifier = Modifier
                .background(RemindTheme.colors.bgPrimary)
                .padding(horizontal = Spacing.screenHorizontal)
        )
    }
}

@PreviewLightDark
@Composable
private fun TaskListEmptyPreview() {
    RemindMeTheme {
        TaskList(
            tasks = emptyList(),
            onToggle = {},
            onClick = {},
            modifier = Modifier
                .background(RemindTheme.colors.bgPrimary)
                .padding(horizontal = Spacing.screenHorizontal)
        )
    }
}
