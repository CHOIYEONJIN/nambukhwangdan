package com.example.nambukhwangdan.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nambukhwangdan.navigation.MainBottomNavigation
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.screens.diary.AnalyzeResultScreen
import com.example.nambukhwangdan.screens.diary.DiaryWriteScreen
import com.example.nambukhwangdan.screens.diary.LetterToTomorrowScreen
import com.example.nambukhwangdan.screens.inbox.InboxScreen
import com.example.nambukhwangdan.screens.journal.JournalScreen
import com.example.nambukhwangdan.screens.letters.NewLetterScreen
import com.example.nambukhwangdan.ui.settings.SettingsScreen
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.viewmodel.AuthViewModel
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import com.example.nambukhwangdan.viewmodel.LetterViewModel

// TODO: 나머지 탭 화면 (Journal, Inbox, Settings)은 더미 파일 사용

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreenHost(
    appNavController: NavController,
    diaryViewModel: DiaryViewModel,
    letterViewModel: LetterViewModel,
    authViewModel: AuthViewModel
) {
    val bottomNavController = rememberNavController()
    MainBottomNavigation(navController = bottomNavController)

    Scaffold(
        containerColor = Background,

        bottomBar = {
            // 둥근 모서리 배경을 위한 Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    // 둥근 모서리 적용
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    // 네비게이션 바가 올라갈 배경색 (예: 흰색)
                    .background(Color.White)
            ) {
                // 투명 배경을 가진 네비게이션 컨텐츠 배치
                MainBottomNavigation(navController = bottomNavController)
            }
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
            composable(Routes.Calendar) { EmotionCalendarScreen(
                diaryViewModel = diaryViewModel,
                letterViewModel=letterViewModel
            ) }

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
                    viewModel = letterViewModel,
                    navController = bottomNavController
                )
            }
            composable(Routes.NewLetter){
                NewLetterScreen(
                    viewModel=letterViewModel,
                    bottomNavController=bottomNavController
                )
            }

            // 더미 화면
            composable(Routes.Inbox) { InboxScreen() }
            composable(Routes.Settings) {
                SettingsScreen(
                    authViewModel = authViewModel, // AuthViewModel 전달
                    onNavigateToLogin = { // 로그아웃 시 최상위 내비게이션(appNavController) 처리
                        appNavController.navigate(Routes.Login) {
                            popUpTo(Routes.MainHost) { inclusive = true } // MainHost 스택 제거
                        }
                    }
                )
            }
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