package com.example.nambukhwangdan.screens.diary

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.nambukhwangdan.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterToTomorrowScreen(
    viewModel: DiaryViewModel,
    navController: NavController,
){
    var TextForTommorow by remember { mutableStateOf( "") }

    Column {
        Text("내일의 나에게 보내는 편지 화면")
        TextField(
            value = TextForTommorow, onValueChange = { TextForTommorow = it }, label = { Text("내일 보낼 일기 내용을 입력해주세요") }
        )
        Button({navController.navigate("HomeScreen")}) {
            Text("저장하기")
        }

    }
}