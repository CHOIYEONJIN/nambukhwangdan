package com.example.nambukhwangdan.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nambukhwangdan.screens.calender.EmotionCalendarScreen
import com.example.nambukhwangdan.screens.diary.AnalyzeLoadingOverlay
import com.example.nambukhwangdan.screens.diary.AnalyzeResultScreen
import com.example.nambukhwangdan.screens.diary.DiaryWriteScreen
import com.example.nambukhwangdan.screens.diary.LetterToTomorrowScreen
import com.example.nambukhwangdan.screens.home.HomeScreen
import com.example.nambukhwangdan.screens.journal.JournalScreen
import com.example.nambukhwangdan.screens.letters.NewLetterScreen
import com.example.nambukhwangdan.screens.letters.ReplyScreen
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import com.example.nambukhwangdan.viewmodel.LetterViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val diaryViewModel: DiaryViewModel = viewModel()
    val letterViewModel: LetterViewModel = viewModel()

    // NOTE: startDestination을 "DiaryWriteScreen"으로 설정하는 것은 이 NavGraph가
    // MainScreenHost 내에서 사용될 때 DiaryWrite가 초기 화면이어야 함을 의미합니다.
    NavHost(navController = navController, startDestination = "DiaryWriteScreen") {

        composable("list") {
            JournalScreen(
                navController = navController, // 👈 필수 매개변수 전달
                viewModel = diaryViewModel     // 👈 ViewModel 전달
            )
        }

        composable (route="EmotionCalendarScreen") {
            EmotionCalendarScreen()
        }

        composable(route="AnalyzeLoadingScreen") {
            AnalyzeLoadingOverlay()
        }

        composable(route="AnalyzeResultScreen"){
            AnalyzeResultScreen(
                viewModel = diaryViewModel,
                // AnalyzeResultScreen은 MainHost 내부의 탭 플로우이므로 bottomNavController를 전달
                bottomNavController = navController
            )
        }

        composable("NewLetterScreen"){
            NewLetterScreen(
                viewModel = letterViewModel,
                bottomNavController = navController
            )
        }

        composable("ReplyScreen"){
            ReplyScreen(
                viewModel = letterViewModel,
                navController = navController
            )
        }

        composable("DiaryWriteScreen/{letterId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("letterId")

            id?.let {
                LaunchedEffect(it) {
                    // viewModel.loadReplyLetterById(it) // TODO: 필요하면 주석 해제
                }
            }

            DiaryWriteScreen(
                viewModel = diaryViewModel,
                bottomNavController = navController
            )
        }

        composable("LetterToTomorrowScreen"){
            LetterToTomorrowScreen(
                viewModel = diaryViewModel,
                navController = navController
            )
        }

        // 일기 쓰기 기본 화면
        composable("DiaryWriteScreen") {
            DiaryWriteScreen(
                viewModel = diaryViewModel,
                bottomNavController = navController
            )
        }

    }
}