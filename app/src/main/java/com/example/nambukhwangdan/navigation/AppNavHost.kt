package com.example.nambukhwangdan.navigation

// TODO: 실제 Onboarding/Login 화면 import
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nambukhwangdan.ui.home.MainScreenHost
import com.example.nambukhwangdan.ui.onboarding.LoginScreen
import com.example.nambukhwangdan.ui.onboarding.OnboardingIntroScreen
import com.example.nambukhwangdan.ui.onboarding.OnboardingNicknameScreen
import com.example.nambukhwangdan.viewmodel.DiaryViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val diaryViewModel: DiaryViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.OnboardingIntro
    ) {
        composable(Routes.OnboardingIntro) {
            OnboardingIntroScreen(
                onNext = { navController.navigate(Routes.OnboardingNickname) },
            )
        }
        composable(Routes.OnboardingNickname) {
            OnboardingNicknameScreen(
                onSubmit = { navController.navigate(Routes.Login) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 시 MainHost로 이동하며 온보딩 스택 제거
                    navController.navigate(Routes.MainHost) {
                        popUpTo(0)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 메인 쉘 연결: 하단 탭 구조를 포함한 MainScreenHost 호출
        composable(Routes.MainHost) {
            MainScreenHost(
                appNavController = navController,   // 👈 AppNav 내려줌
                diaryViewModel = diaryViewModel
            )
        }

    }
}