package com.example.nambukhwangdan.screens.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nambukhwangdan.viewmodel.DiaryViewModel

@Composable

fun OnboardingNickname(viewModel: DiaryViewModel,navController: NavController){
    var nickname by remember { mutableStateOf("") }
    Column {
        Text("닉네임 입력 화면")
        Spacer(modifier = Modifier.padding(16.dp))
        TextField(value = nickname,
            onValueChange = { nickname = it }, label = { Text("닉네임을 입력해주세요") },
            modifier = Modifier.fillMaxWidth().padding(8.dp))
        Button (onClick= { navController.navigate("LoginScreen") }){
            Text("다음")
        }
    }
}