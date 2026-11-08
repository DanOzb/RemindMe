package com.example.remindme.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.remindme.ui.theme.Spacing
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp

@Composable
fun LargeHeaderTitle(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
        contentAlignment = Alignment.BottomStart
    ) {
        ScreenHeader(text)
    }
}

@PreviewLightDark
@Composable
private fun LargeHeaderTitlePreview() {
    PreviewSurface {
        LargeHeaderTitle("Account & settings", Modifier.height(96.dp))
    }
}
