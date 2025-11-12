package com.example.nambukhwangdan.screens.diary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import kotlinx.coroutines.delay

@Composable
fun AnalyzeLoadingScreen(
    viewModel: DiaryViewModel,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        viewModel.runAnalyze()
        delay(1500) // 실제 API라면 응답 타이밍에 맞춰 navigate
        navController.navigate("AnalyzeResultScreen")
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("오늘의 감정을 분석중이에요", fontWeight = FontWeight.SemiBold)
        }
    }
}