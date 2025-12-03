package com.example.nambukhwangdan.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nambukhwangdan.ui.theme.*  // Background, Primary, Surface, OnSurface, Grey
import androidx.compose.foundation.clickable

@Composable
fun OnboardingIntroScreen(
    onNext: () -> Unit = {},
) {
    val pretendard = FontFamily.Default // TODO: Pretendard 폰트 리소스 추가 시 교체

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // spacer 150
            Spacer(Modifier.height(150.dp))

            // 흰색 박스 160x80 (로고 자리)
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface)
            )

            // spacer 20
            Spacer(Modifier.height(20.dp))

            // 텍스트: '띄워보내는'만 Primary, 나머지 #000000
            val title = buildAnnotatedString {
                withStyle(SpanStyle(color = Primary)) { append("띄워보내는") }
                withStyle(SpanStyle(color = OnSurface)) { append(" 에 오신것을 환영합니다") }
            }
            Text(
                text = title,
                fontFamily = pretendard,
                fontSize = 18.sp,
                lineHeight = 20.sp
            )

            // spacer 80
            Spacer(Modifier.height(80.dp))

            // 안내 텍스트 3줄 (필요 문구로 교체)
            Text(
                text = "내일의 나와 함께 하루를 기록해요",
                fontFamily = pretendard,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = OnSurface
            )
            // spacer 20
            Spacer(Modifier.height(20.dp))
            Text(
                text = "특정 날짜에 도착하는 느린 편지를 만들 수 있어요",
                fontFamily = pretendard,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = OnSurface
            )
            // spacer 20
            Spacer(Modifier.height(20.dp))
            Text(
                text = "익명에게 띄우는 유리병 편지도 가능해요",
                fontFamily = pretendard,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = OnSurface
            )

            // spacer 250
            Spacer(Modifier.weight(1f))
            // 인디케이터 Row: 긴 pill(30x10, Primary) - 10dp - 원(10x10) - 10dp - 원(10x10)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 30.dp, height = 10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Primary)
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Grey)
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Grey)
                )
            }

            // spacer 66
            Spacer(Modifier.height(56.dp))

            // 하단 큰 버튼: width=393, height=85, radius=10, 가운데 텍스트
            // (실기기 폭이 393보다 좁을 수 있어 fillMaxWidth 사용 권장)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 10.dp,  // 좌상단
                            topEnd = 10.dp,    // 우상단
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(Primary)
                    .clickable { onNext() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "다음",
                    color = Surface,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingIntro_NoCanvas_Preview() {
    MaterialTheme { // 최소 테마
            OnboardingIntroScreen(
                onNext = {}
            )

    }
}