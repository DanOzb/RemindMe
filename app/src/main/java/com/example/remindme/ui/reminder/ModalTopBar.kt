package com.example.remindme.ui.reminder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.PreviewSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalTopBar(
    title: String,
    onBack: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RemindTheme.colors

    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.text)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
            }
        },
        actions = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.text)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.bgPrimary)
    )
}

@PreviewLightDark
@Composable
private fun ModalTopBarPreview() {
    PreviewSurface {
        ModalTopBar(title = "New reminder", onBack = {}, onClose = {})
    }
}
