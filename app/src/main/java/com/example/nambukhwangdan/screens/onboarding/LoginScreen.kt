package com.example.nambukhwangdan.screens.onboarding

import android.R.attr.onClick
import android.R.attr.text
import androidx.compose.foundation.layout.Column
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
import com.example.nambukhwangdan.model.Diary.Diary
import com.example.nambukhwangdan.viewmodel.DiaryViewModel

@Composable
fun LoginScreen(viewModel: DiaryViewModel,navController: NavController){
    Column(){
        Text("로그인 화면")
        Button(onClick ={} ){
            Text("로그인 버튼")
        }
        Button(onClick={navController.navigate("HomeScreen")}){
            Text("시작하기")
        }
    }
}