package com.example.nambukhwangdan.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nambukhwangdan.ui.theme.*  // Background, Primary, Surface, OnSurface, Grey

fun Modifier.swipeBack(
    onBack: () -> Unit,
    edgeOnly: Boolean = true,   // 왼쪽 가장자리에서만 시작 허용
    edgeWidth: Dp = 24.dp,      // 가장자리 폭
    triggerDistance: Dp = 64.dp // 최소 드래그 거리
): Modifier = composed {
    val density = LocalDensity.current
    val edgePx = with(density) { edgeWidth.toPx() }
    val triggerPx = with(density) { triggerDistance.toPx() }

    var startedAtEdge = false
    var totalDrag = 0f

    pointerInput(onBack) {
        detectHorizontalDragGestures(
            onDragStart = { offset ->
                startedAtEdge = if (edgeOnly) offset.x <= edgePx else true
                totalDrag = 0f
            },
            onHorizontalDrag = { _, dragAmount ->
                if (!startedAtEdge) return@detectHorizontalDragGestures
                if (dragAmount > 0f) totalDrag += dragAmount // 오른쪽으로만 누적
            },
            onDragEnd = {
                if (startedAtEdge && totalDrag >= triggerPx) onBack()
            }
        )
    }
}
@Composable
fun OnboardingNicknameScreen(
    onSubmit: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val pretendard = FontFamily.Default // TODO: Pretendard 폰트 리소스 추가 시 교체
    var nickname by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .swipeBack(onBack = onBack)
            .background(Background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // spacer 150
            Spacer(Modifier.height(150.dp))

            // 제목: '띄워보내는'만 Primary
            val title = buildAnnotatedString {
                withStyle(SpanStyle(color = Primary)) { append("띄워보내는") }
                withStyle(SpanStyle(color = OnSurface)) { append(" 에서 사용할 닉네임을 정해주세요") }
            }
            Text(
                text = title,
                fontFamily = pretendard,
                fontSize = 18.sp,
                lineHeight = 20.sp
            )

            // spacer 30
            Spacer(Modifier.height(30.dp))

            // 텍스트 입력 박스 250*35, 언더라인(1dp, OnSurface), 플레이스홀더= '익명의 작성자'(Primary)
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(35.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = OnSurface,
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                        fontFamily = pretendard
                    ),
                    cursorBrush = SolidColor(Primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp), // 언더라인과 간격
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxWidth()) {
                            if (nickname.isEmpty()) {
                                Text(
                                    text = "익명의 작성자",
                                    color = Primary,
                                    fontFamily = pretendard,
                                    fontSize = 18.sp,
                                    lineHeight = 20.sp
                                )
                            }
                            inner()
                        }
                    }
                )
                // Understroke (1dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(OnSurface)
                )
            }

            // spacer 20
            Spacer(Modifier.height(20.dp))

            // 안내 텍스트 (14sp, 높이 20 기준)
            Text(
                text = "나중에 설정에서 수정할 수 있어요",
                fontFamily = pretendard,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = OnSurface
            )

            // spacer 425
            Spacer(Modifier.weight(1f))

            // 인디케이터: 원(10) - 10 - pill(30x10, Primary) - 10 - 원(10)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Grey)
                )
                Spacer(Modifier.width(10.dp))
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
            }

            // spacer 56
            Spacer(Modifier.height(56.dp))

            // 하단 버튼: width=393 대신 fillMaxWidth 권장, height=85, 상단만 10dp 둥글게
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
                    .clickable { onSubmit() },
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
private fun OnboardingNicknameScreen_Preview() {
    MaterialTheme { // 최소 테마
        OnboardingNicknameScreen(
            onSubmit = {},
            onBack = {}
        )

    }
}