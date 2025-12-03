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
import com.example.nambukhwangdan.screens.onboarding.LoginScreen
import com.example.nambukhwangdan.screens.onboarding.OnboardingIntroScreen
import com.example.nambukhwangdan.screens.onboarding.OnboardingNickname
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import com.example.nambukhwangdan.viewmodel.LetterViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val diaryViewModel: DiaryViewModel = viewModel()
    val letterViewModel: LetterViewModel = viewModel()

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
                // ⭐️ [수정] 인자 이름을 bottomNavController로 변경하여 Compose 함수의 정의와 일치시킵니다.
                bottomNavController = navController
            )
        }

        // NOTE: OnboardingIntroScreen과 OnboardingNickname도 navController와 ViewModel을
        // 요구할 가능성이 높으므로 인자를 명시적으로 전달합니다. (JournalScreen과 동일 패턴 적용)
        composable(route="OnboardingScreen"){
            OnboardingIntroScreen(
                viewModel = diaryViewModel,
                navController = navController
            )
        }

        composable(route="OnboardingNicknameScreen"){
            OnboardingNickname(
                viewModel = diaryViewModel,
                navController = navController
            )
        }

        // ✅ [수정] LoginScreen은 onLoginSuccess 람다를 받도록 수정 (제공된 LoginScreen.kt 파일 참조)
        composable(route="LoginScreen"){
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 시 DiaryWriteScreen으로 이동하고, 로그인 화면을 백스택에서 제거
                    navController.navigate("DiaryWriteScreen") {
                        popUpTo("LoginScreen") { inclusive = true }
                    }
                },
                // 뒤로 가기 동작 (여기서는 이전 화면인 OnboardingIntroScreen으로 돌아가거나 닫기)
                onBack = { navController.popBackStack() }
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
                viewModel = letterViewModel,
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

        // NOTE: HomeScreen이 누락되어 있어 추가합니다. (이름은 임의로 추정)
        composable("HomeScreen") {
            // HomeScreen도 NavController나 ViewModel이 필요할 수 있습니다.
            HomeScreen(navController = navController)
        }
    }
}