package com.example.remindme.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.remindme.ui.home.HomeScreen
import com.example.remindme.ui.profile.ProfileScreen
import com.example.remindme.ui.theme.RemindTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.remindme.ui.theme.RemindMeTheme

@Composable
fun MainScreen(
    onNewReminder: () -> Unit,
    onEditReminder: (Int) -> Unit
) {
    val pages = MainPage.entries
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    // Back on Account goes to Home instead of closing the app
    BackHandler(enabled = pagerState.currentPage != MainPage.Home.ordinal) {
        scope.launch { pagerState.animateScrollToPage(MainPage.Home.ordinal) }
    }

    Scaffold(
        containerColor = RemindTheme.colors.bgPrimary,
        bottomBar = {
            RemindBottomBar(
                pages = pages,
                selected = pages[pagerState.currentPage],
                onSelect = { page -> scope.launch { pagerState.animateScrollToPage(page.ordinal) } }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            key = { pages[it].name }
        ) { index ->
            when (pages[index]) {
                MainPage.Home -> HomeScreen(
                    onNewReminder = onNewReminder,
                    onEditReminder = onEditReminder
                )
                MainPage.Account -> ProfileScreen()
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MainScreenPreview() {
    RemindMeTheme {
        MainScreen(onNewReminder = {}, onEditReminder = {})
    }
}
