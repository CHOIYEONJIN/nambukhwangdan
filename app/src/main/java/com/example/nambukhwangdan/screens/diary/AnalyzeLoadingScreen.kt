package com.example.nambukhwangdan.screens.diary

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.nambukhwangdan.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeLoadingScreen(
    viewModel: DiaryViewModel,
    navController: NavController){
    Column{
        Text("감정 분석 화면")
    //아마 process 처리를 %로 보여주는 명령어가 있을텐데 뭔지 모르겠어서 일단 비워뒀어요
        Text("감정을 분석중이에요")
        //원래 분석이 끝나면 자동으로 넘어가는데 아직 적용을 안시켰으니 버튼으로 구현했습니다.
        Button({navController.navigate("AnalyzeResultScreen")}){
            Text("다음 화면으로 넘어가기")
        }
    }
}