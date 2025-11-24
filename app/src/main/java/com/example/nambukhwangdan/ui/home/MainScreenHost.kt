package com.example.nambukhwangdan.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nambukhwangdan.navigation.MainBottomNavigation
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.screens.diary.AnalyzeResultScreen
import com.example.nambukhwangdan.screens.diary.DiaryWriteScreen
import com.example.nambukhwangdan.screens.diary.LetterToTomorrowScreen
import com.example.nambukhwangdan.screens.journal.JournalScreen
import com.example.nambukhwangdan.screens.letters.NewLetterScreen
import com.example.nambukhwangdan.viewmodel.DiaryViewModel

// TODO: 나머지 탭 화면 (Journal, Inbox, Settings)은 더미 파일 사용

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreenHost(
    appNavController: NavController,      // 👈 AppNavHost에서 전달받는 NavController
    diaryViewModel: DiaryViewModel        // 👈 AppNavHost에서 생성된 ViewModel도 같이 전달
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            MainBottomNavigation(navController = bottomNavController)
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 홈 화면
            composable(Routes.Home) {
                HomeScreen(
                    viewModel = diaryViewModel,
                    bottomNavController = bottomNavController)
            }

            // 감정 캘린더
            composable(Routes.Calendar) { EmotionCalendarScreen() }

            // 일기 탭
            composable(Routes.Journal) {
                JournalScreen(
                    viewModel = diaryViewModel
                )
            }

            composable(Routes.DiaryWrite) {
                DiaryWriteScreen(
                    viewModel = diaryViewModel,
                    bottomNavController = bottomNavController
                )
            }


            composable(Routes.AnalyzeResult) {
                AnalyzeResultScreen(
                    viewModel = diaryViewModel,
                    bottomNavController = bottomNavController
                )
            }

            composable(Routes.LetterToTomorrow) {
                LetterToTomorrowScreen(
                    viewModel = diaryViewModel,
                    navController = bottomNavController
                )
            }
            composable(Routes.NewLetter){
                NewLetterScreen(
                    viewModel=diaryViewModel,
                    bottomNavController=bottomNavController
                )
            }

            // 더미 화면
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