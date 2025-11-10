package com.example.nambukhwangdan.screens.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.nambukhwangdan.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeResultScreen(
    viewModel: DiaryViewModel,
    navController: NavController
){
    Column {
        Text("감정 분석 결과 화면")
        Text("오늘 작성한 일기 내용")
        Row {
            Button({}, modifier = Modifier.background(Color.Red)) {
            }
            Button({}, modifier = Modifier.background(Color.Yellow)) { }
            Button({}, modifier = Modifier.background(Color.Blue)) { }

        }
        Button(onClick = { navController.navigate("LetterToTomorrowScreen") }) {
            Text("다음으로")
        }

    }

}