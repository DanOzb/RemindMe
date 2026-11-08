package com.example.remindme.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Scrollbar copied from Medium - Kshitij Mittal and modified to use on lazycolumn
 * https://medium.com/@mittalkshitij20/adding-a-custom-scrollbar-to-a-column-in-jetpack-compose-9996c26f498f
 */
fun Modifier.verticalLazyScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    thumbHeight: Dp? = null,          // null = proportional to content
    minThumbHeight: Dp = 24.dp,
    trackPadding: Dp = 0.dp,          // shortens the track from top and bottom
    showScrollBarTrack: Boolean = true,
    scrollBarTrackColor: Color = Color.Gray,
    scrollBarColor: Color = Color.Black,
    scrollBarCornerRadius: Float = 4f,
    endPadding: Dp = 4.dp
): Modifier {
    return drawWithContent {
        drawContent()

        val info = state.layoutInfo
        val visible = info.visibleItemsInfo
        if (info.totalItemsCount == 0 || visible.isEmpty()) return@drawWithContent

        // LazyColumn doesn't know its total height, so estimate it from the visible items
        val avgItemSize = visible.sumOf { it.size } / visible.size.toFloat()
        val contentHeight = avgItemSize * info.totalItemsCount
        val viewportHeight = size.height

        if (contentHeight <= viewportHeight) return@drawWithContent

        val trackTop = trackPadding.toPx()
        val trackHeight = viewportHeight - 2 * trackPadding.toPx()
        val x = size.width - endPadding.toPx() - width.toPx()

        val proportionalHeight = (viewportHeight / contentHeight) * trackHeight
        val thumbH = (thumbHeight?.toPx() ?: proportionalHeight)
            .coerceAtLeast(minThumbHeight.toPx())
            .coerceAtMost(trackHeight)

        val first = visible.first()
        val scrolled = first.index * avgItemSize - first.offset
        val progress = (scrolled / (contentHeight - viewportHeight)).coerceIn(0f, 1f)
        val thumbTop = trackTop + progress * (trackHeight - thumbH)

        if (showScrollBarTrack) {
            drawRoundRect(
                cornerRadius = CornerRadius(scrollBarCornerRadius),
                color = scrollBarTrackColor,
                topLeft = Offset(x, trackTop),
                size = Size(width.toPx(), trackHeight)
            )
        }

        drawRoundRect(
            cornerRadius = CornerRadius(scrollBarCornerRadius),
            color = scrollBarColor,
            topLeft = Offset(x, thumbTop),
            size = Size(width.toPx(), thumbH)
        )
    }
}
