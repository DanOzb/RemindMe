package com.example.remindme.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.remindme.ui.theme.RemindTheme
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.RemindMeTheme

@Composable
fun RemindBottomBar(
    pages: List<MainPage>,
    selected: MainPage,
    onSelect: (MainPage) -> Unit
) {
    val colors = RemindTheme.colors
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = colors.textBrand,
        selectedTextColor = colors.textBrand,
        indicatorColor = colors.bgGreenCards,
        unselectedIconColor = colors.textSecondary,
        unselectedTextColor = colors.textSecondary
    )

    Column {
        HorizontalDivider(thickness = 1.dp, color = colors.border)
        NavigationBar(
            containerColor = colors.bgPrimary,
            tonalElevation = 0.dp
        ) {
            pages.forEach { page ->
                val isSelected = page == selected
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelect(page) },
                    icon = { Icon(page.icon, contentDescription = null) },
                    label = {
                        Text(
                            text = page.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = itemColors
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun RemindBottomBarPreview() {
    RemindMeTheme {
        RemindBottomBar(pages = MainPage.entries, selected = MainPage.Home, onSelect = {})
    }
}
