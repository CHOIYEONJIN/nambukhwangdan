package com.example.nambukhwangdan.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nambukhwangdan.navigation.MainBottomNavigation
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.ui.home.HomeScreen
import com.example.nambukhwangdan.ui.home.EmotionCalendarScreen
// TODO: 나머지 탭 화면 (Journal, Inbox, Settings)은 더미 파일 사용

@Composable
fun MainScreenHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            // 하단 네비게이션 바를 여기서 고정
            MainBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home, // 첫 시작은 홈 탭
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            // 탭 화면들 연결
            composable(Routes.Home) { HomeScreen() }
            composable(Routes.Calendar) { EmotionCalendarScreen() }

            // 더미 화면 연결
            composable(Routes.Journal) { DummyScreen("일기장") }
            composable(Routes.Inbox) { DummyScreen("편지함") }
            composable(Routes.Settings) { DummyScreen("내 정보") }
        }
    }
}

// 더미 화면 정의 (필요하다면 별도 파일로 분리하세요)
@Composable
fun DummyScreen(title: String) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.White),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(title, color = androidx.compose.ui.graphics.Color.Black)
    }
}