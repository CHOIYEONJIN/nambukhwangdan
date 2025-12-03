package com.example.nambukhwangdan.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nambukhwangdan.ui.theme.* // Background, Primary, Surface, OnSurface, Grey
import com.example.nambukhwangdan.viewmodel.AuthViewModel // AuthViewModel 임포트 추가
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

// 기존 swipeBack 함수 유지
fun Modifier.swipeBack(
    onBack: () -> Unit,
    edgeOnly: Boolean = true,
    edgeWidth: Dp = 24.dp,
    triggerDistance: Dp = 64.dp
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
    // 1. AuthViewModel을 매개변수로 받도록 수정 (AppNavHost에서 전달됨)
    authViewModel: AuthViewModel = viewModel(),
    onSubmit: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val pretendard = FontFamily.Default
    var nickname by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // AuthViewModel의 상태를 관찰
    val authState by authViewModel.authState.collectAsState()

    // 5. 닉네임 저장 성공 시 다음 화면으로 이동
    // 'isNicknameSaved'가 true로 변경될 때 딱 한 번 실행
    LaunchedEffect(authState.isNicknameSaved) {
        if (authState.isNicknameSaved) {
            onSubmit() // LoginScreen으로 이동
        }
    }

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

            Spacer(Modifier.height(30.dp))

            // 텍스트 입력 박스
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(35.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = nickname,
                    onValueChange = {
                        // 최대 15자 제한 (옵션)
                        if (it.length <= 15) nickname = it
                    },
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
                        .padding(bottom = 8.dp),
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxWidth()) {
                            // 플레이스홀더: 닉네임이 비어있을 때만 표시
                            if (nickname.isEmpty()) {
                                Text(
                                    text = "익명의 작성자",
                                    color = Primary.copy(alpha = 0.6f), // 플레이스홀더 색상 조정
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
                        .background(OnSurface.copy(alpha = 0.5f))
                )
            }

            Spacer(Modifier.height(20.dp))

            // 안내 텍스트 (14sp, 높이 20 기준)
            Text(
                text = "나중에 설정에서 수정할 수 있어요",
                fontFamily = pretendard,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = OnSurface.copy(alpha = 0.7f)
            )

            // 저장 중 로딩 또는 오류 메시지 표시
            Spacer(Modifier.height(16.dp))

            if (authState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary)
            }
            authState.saveError?.let { error ->
                Text(
                    text = "저장 실패: 잠시 후 다시 시도해주세요. ($error)",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(250.dp)
                )
            }


            Spacer(Modifier.weight(1f))

            // 인디케이터 (현재 페이지는 2번째: 닉네임 설정)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(Grey))
                Spacer(Modifier.width(10.dp))
                // 현재 페이지 (활성화)
                Box(
                    Modifier
                        .size(width = 30.dp, height = 10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Primary)
                )
                Spacer(Modifier.width(10.dp))
                Box(Modifier.size(10.dp).clip(CircleShape).background(Grey))
            }

            Spacer(Modifier.height(56.dp))

            // 하단 버튼: width=393 대신 fillMaxWidth 권장, height=85, 상단만 10dp 둥글게
            // 4. 버튼 활성화/비활성화 및 로딩 상태 처리
            val isButtonEnabled = nickname.isNotBlank() && !authState.isSaving
            val buttonColor = if (isButtonEnabled) Primary else Primary.copy(alpha = 0.5f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(85.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(buttonColor)
                    // 3. 닉네임 저장 로직 연결
                    .clickable(enabled = isButtonEnabled) {
                        scope.launch {
                            authViewModel.saveNickname(nickname)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (authState.isSaving) "저장 중..." else "다음",
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
    MaterialTheme {
        // Preview에서는 기능 없이 UI만 보여줍니다.
        // OnboardingNicknameScreen(authViewModel = viewModel(), onSubmit = {}, onBack = {}) // 컴파일 오류 방지
        Text("OnboardingNicknameScreen Preview")

    }
}