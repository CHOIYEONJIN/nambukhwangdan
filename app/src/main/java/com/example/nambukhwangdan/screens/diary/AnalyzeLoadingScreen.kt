package com.example.nambukhwangdan.screens.diary

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import kotlinx.coroutines.delay

@Composable
fun AnalyzeLoadingOverlay(
    viewModel: DiaryViewModel,
    bottomNavController: NavController
) {
    LaunchedEffect(Unit) {
        delay(150)
        // 호출 뒤 navigate → 결과 화면으로 이동
        bottomNavController.navigate(Routes.AnalyzeResult)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),  // ← 반투명 배경
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("오늘의 감정을 분석중이에요", fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}
