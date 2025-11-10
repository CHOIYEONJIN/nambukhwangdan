package com.example.nambukhwangdan.screens.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.nambukhwangdan.model.Diary
import com.example.nambukhwangdan.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryWriteScreen(
    viewModel: DiaryViewModel,
    navController: NavController,
){
    var content by remember { mutableStateOf( "") }
    Text("일기 작성 화면")
    Column{
        Column(Modifier.background(color=Color(0xFFDBE4ED))){
        Text("과거 일기 내용")
        }
       Column (modifier = Modifier.background(color=Color(0xFFFFFAF1))){
            Text("오늘 작성할 일기 내용")
           Button(onClick={}){//갤러리 접근 방법을 몰라서 화면을 만들어야하는지 모르겠네여 일단 비워뒀습니다
               Text("사진 선택")
           }
           TextField(value = content,
                onValueChange = { content = it }, label = { Text("오늘의 일기 내용을 입력해주세요") })
           Button({navController.navigate("AnalyzeLoadingScreen")}){
               Text("다음으로")
           }
        }
    }
}