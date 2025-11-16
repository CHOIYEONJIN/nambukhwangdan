package com.example.nambukhwangdan.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nambukhwangdan.ui.theme.*


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},   // 구글 버튼 클릭 시
    onBack: () -> Unit = {}     // 하단 “시작하기” 클릭 시
) {
    val pretendard = FontFamily.Default

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .swipeBack(onBack = onBack),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(120.dp))

        Text(
            text = "로그인을 해주세요",
            fontSize = 20.sp,
            color = OnSurface,
            fontFamily = pretendard
        )

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 50.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .clickable { onLoginSuccess() },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .size(width = 300.dp, height = 50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 14.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Google 계정으로 계속하기",
                    color = Color(0xFF3C4043),
                    fontSize = 16.sp,
                    fontFamily = pretendard
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "계속을 클릭하면 당사의 서비스 이용 약관 및 개인정보 처리 방침에 동의하는 것으로 간주됩니다.",
            color = Color(0xFF6B7BA0),
            modifier = Modifier.width(300.dp),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(Grey))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(10.dp).clip(CircleShape).background(Grey))
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .size(width = 30.dp, height = 10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Primary)
            )

        }

        Spacer(Modifier.height(56.dp))

        // 하단 버튼(상단만 둥글게)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp, topEnd = 10.dp,
                        bottomStart = 0.dp, bottomEnd = 0.dp
                    )
                )
                .background(Primary)
                .clickable { onLoginSuccess() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "시작하기",
                color = Surface,
                fontSize = 18.sp,
                fontFamily = pretendard
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=393dp,height=852dp,dpi=440")
@Composable
private fun LoginScreeny_Preview() {
    MaterialTheme {
        LoginScreen(
            onLoginSuccess = {},
            onBack = {}
        )
    }
}