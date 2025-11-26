package com.example.nambukhwangdan.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.nambukhwangdan.viewmodel.DiaryViewModel


@Composable
fun HomeScreen(
    viewModel: DiaryViewModel,navController: NavController){
    Column{
    Text("홈 화면 (책상)")
    Button(onClick = {navController.navigate("DiaryWriteScreen")}){
        Text("답장하기")
        // 아직 팝업화면 구성하는 방법을 몰라서 일단 한 화면에 두 버튼 다 둠
    }
    Button(onClick={navController.navigate("NewLetterScreen")}){
        Text("편지 쓰러가기")}
    }
}