package com.example.nambukhwangdan.screens.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun AnalyzeLoadingOverlay() {

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Readabook.json"))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // ⚠️ [교체] CircularProgressIndicator 대신 LottieAnimation 사용
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever, // 애니메이션 무한 반복
                // ⭐️ 애니메이션의 크기를 적절히 지정합니다. (예: 120dp)
                modifier = Modifier.size(180.dp)
            )

            Spacer(Modifier.height(12.dp))
            Text(
                "오늘의 감정을 분석중이에요...",
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}