package com.example.remindme.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.RemindTheme
import com.example.remindme.ui.theme.Spacing
import kotlin.math.roundToInt
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.RemindMeTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn

/** [content] must be scrollable for the header to react. */
@Composable
fun CollapsingHeaderLayout(
    collapsedTitle: String,
    modifier: Modifier = Modifier,
    expandedHeight: Dp = 120.dp,
    actions: @Composable RowScope.() -> Unit = {},
    expandedContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val maxOffsetPx = with(LocalDensity.current) { expandedHeight.toPx() }
    val state = remember(maxOffsetPx) { CollapsingHeaderState(maxOffsetPx) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(state.nestedScrollConnection)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .layout { measurable, constraints ->
                    val fullHeight = maxOffsetPx.roundToInt()
                    val placeable = measurable.measure(
                        constraints.copy(minHeight = fullHeight, maxHeight = fullHeight)
                    )
                    val visibleHeight = (fullHeight + state.offsetPx).roundToInt().coerceAtLeast(0)
                    layout(placeable.width, visibleHeight) {
                        placeable.place(0, visibleHeight - fullHeight)
                    }
                }
                .graphicsLayer {
                    alpha = (1f - state.collapsedFraction * 1.5f).coerceIn(0f, 1f)
                }
        ) {
            expandedContent()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = Spacing.screenHorizontal, end = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = collapsedTitle,
                style = MaterialTheme.typography.titleLarge,
                color = RemindTheme.colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { alpha = state.collapsedFraction }
            )
            actions()
        }

        Box(Modifier.weight(1f)) {
            content()
        }
    }
}

@PreviewLightDark
@Composable
private fun CollapsingHeaderLayoutPreview() {
    RemindMeTheme {
        CollapsingHeaderLayout(
            collapsedTitle = "Title",
            modifier = Modifier.background(RemindTheme.colors.bgPrimary),
            expandedContent = { LargeHeaderTitle("Title") }
        ) {
            LazyColumn(Modifier.padding(horizontal = Spacing.screenHorizontal)) {
                items(20) { index ->
                    Text(
                        text = "Row ${index + 1}",
                        color = RemindTheme.colors.text,
                        modifier = Modifier.padding(vertical = Spacing.sm)
                    )
                }
            }
        }
    }
}
