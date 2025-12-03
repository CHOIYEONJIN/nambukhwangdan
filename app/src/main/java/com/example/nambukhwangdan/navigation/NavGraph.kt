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
import com.example.nambukhwangdan.screens.journal.JournalScreen
import com.example.nambukhwangdan.screens.letters.NewLetterScreen
import com.example.nambukhwangdan.screens.letters.ReplyScreen
import com.example.nambukhwangdan.screens.onboarding.LoginScreen
import com.example.nambukhwangdan.screens.onboarding.OnboardingIntroScreen
import com.example.nambukhwangdan.screens.onboarding.OnboardingNickname
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import com.example.nambukhwangdan.viewmodel.LetterViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    //navController가 이동할 수 있게 각 route별 이동 위치를 지정함
    // 상태 private 리스트인 todolist에 접근할 수 있는 viewmodel을 함께 제공함
    // -> viewmodel없이는 todolist의 데이터를 알 수 없음
    val DiaryViewModel: DiaryViewModel = viewModel()
    val LetterViewModel: LetterViewModel = viewModel()
    NavHost(navController = navController, startDestination = "DiaryWriteScreen") {
        // route 가 list일 떄 TodoListScreen으로 이동함
        // 할 일 목록 화면으로 이동한다
        composable("list") {
            JournalScreen(DiaryViewModel)
        }
        // route 가 addEdit일 때 AddEditTodoScreen으로 이동함
        // 새로운 todo를 만드는 화면으로 이동할 때 사용하는 route
        composable (route="EmotionCalendarScreen") {
            EmotionCalendarScreen()
        }
        composable(route="AnalyzeLoadingScreen") {
            AnalyzeLoadingOverlay()
        }
        composable(route="AnalyzeResultScreen"){
            AnalyzeResultScreen(DiaryViewModel,navController)
        }
        composable(route="OnboardingScreen"){
            OnboardingIntroScreen(DiaryViewModel,navController)
        }
        composable(route="OnboardingNicknameScreen"){
            OnboardingNickname(DiaryViewModel,navController)
        }
        composable(route="LoginScreen"){
            LoginScreen(DiaryViewModel,navController)
        }

        composable("NewLetterScreen"){
            NewLetterScreen(LetterViewModel,navController)
        }
        composable("ReplyScreen"){
            ReplyScreen(LetterViewModel,navController)
        }
        composable("DiaryWriteScreen/{letterId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("letterId")

            id?.let {
                LaunchedEffect(it) {
                    //viewModel.loadReplyLetterById(it)
                }
            }

            DiaryWriteScreen(
                viewModel = DiaryViewModel,
                bottomNavController = navController
            )
        }
        composable("LetterToTomorrowScreen"){
            LetterToTomorrowScreen(LetterViewModel,navController)
        }

        composable("DiaryWriteScreen") {
            DiaryWriteScreen(DiaryViewModel, navController)
        }
    }
}

