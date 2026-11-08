package com.example.remindme.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/** offsetPx goes from 0 (fully expanded) to -maxOffsetPx (fully collapsed). */
@Stable
class CollapsingHeaderState(private val maxOffsetPx: Float) {
    var offsetPx by mutableFloatStateOf(0f)
        private set

    /** 0 = expanded, 1 = collapsed */
    val collapsedFraction: Float
        get() = if (maxOffsetPx <= 0f) 0f else -offsetPx / maxOffsetPx

    private fun consume(delta: Float): Float {
        val old = offsetPx
        offsetPx = (old + delta).coerceIn(-maxOffsetPx, 0f)
        return offsetPx - old
    }

    /** After the finger lifts, snap fully open or fully closed so it never rests halfway. */
    private suspend fun settle() {
        val target = if (collapsedFraction > 0.5f) -maxOffsetPx else 0f
        if (offsetPx != target) {
            animate(initialValue = offsetPx, targetValue = target) { value, _ -> offsetPx = value }
        }
    }

    val nestedScrollConnection = object : NestedScrollConnection {
        // Scrolling up: collapse the header first, then let the list scroll.
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
            if (available.y < 0f) Offset(0f, consume(available.y)) else Offset.Zero

        // Scrolling down: the list scrolls back to the top first, then the header expands.
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
            if (available.y > 0f) Offset(0f, consume(available.y)) else Offset.Zero

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            settle()
            return Velocity.Zero
        }
    }
}
