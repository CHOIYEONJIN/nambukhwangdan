package com.example.nambukhwangdan.navigation

// TODO: 실제 Onboarding/Login 화면 import
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nambukhwangdan.ui.home.MainScreenHost
import com.example.nambukhwangdan.ui.onboarding.LoginScreen
import com.example.nambukhwangdan.ui.onboarding.OnboardingIntroScreen
import com.example.nambukhwangdan.ui.onboarding.OnboardingNicknameScreen
import com.example.nambukhwangdan.viewmodel.AuthViewModel
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import com.example.nambukhwangdan.viewmodel.LetterViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nambukhwangdan.MainActivity
import android.Manifest

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val diaryViewModel: DiaryViewModel = viewModel()
    val letterViewModel: LetterViewModel =viewModel()
    val authState by authViewModel.authState.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        // Activity와 Build 버전 확인
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            // 권한이 부여되지 않았는지 확인
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

                // 권한 요청 대화상자 표시
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    MainActivity.REQUEST_CODE_NOTIFICATION_PERMISSION
                )
            }
        }
    }
    if (authState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
        NavHost(
            navController = navController,
            startDestination = authState.startDestination
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
                    diaryViewModel = diaryViewModel,
                    authViewModel=authViewModel, // 👈 AuthViewModel
                    letterViewModel= letterViewModel
                )
            }

        }}
