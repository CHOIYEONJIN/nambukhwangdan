package com.example.nambukhwangdan.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import com.example.nambukhwangdan.screens.onboarding.OnboardingNickname
@Composable
fun OnboardingIntroScreen(
    viewModel: DiaryViewModel,
    navController: NavController){
    Column{
        Text("맨 처음 시작화면")
        Button(onClick = { navController.navigate("OnboardingNicknameScreen") }){
            Text("다음")
        }
    }
}