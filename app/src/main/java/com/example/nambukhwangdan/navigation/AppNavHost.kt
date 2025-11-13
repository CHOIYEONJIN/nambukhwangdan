package com.example.nambukhwangdan.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nambukhwangdan.ui.home.MainScreenHost
// TODO: 실제 Onboarding/Login 화면 import
import com.example.nambukhwangdan.ui.onboarding.OnboardingIntroScreen
import com.example.nambukhwangdan.ui.onboarding.OnboardingNicknameScreen
import com.example.nambukhwangdan.ui.onboarding.LoginScreen

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.OnboardingIntro
    ) {
        composable(Routes.OnboardingIntro) {
            OnboardingIntroScreen(
                onNext = { nav.navigate(Routes.OnboardingNickname) },
            )
        }
        composable(Routes.OnboardingNickname) {
            OnboardingNicknameScreen(
                onSubmit = { nav.navigate(Routes.Login) },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 시 MainHost로 이동하며 온보딩 스택 제거
                    nav.navigate(Routes.MainHost) {
                        popUpTo(0)
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }

        // 메인 쉘 연결: 하단 탭 구조를 포함한 MainScreenHost 호출
        composable(Routes.MainHost) {
            MainScreenHost()
        }
    }
}