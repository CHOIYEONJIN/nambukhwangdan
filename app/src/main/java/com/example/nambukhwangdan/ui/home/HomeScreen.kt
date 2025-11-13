package com.example.nambukhwangdan.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nambukhwangdan.ui.theme.*
import kotlin.random.Random
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog

@Composable
fun HomeScreen() {
    val pretendard = FontFamily.Default
    val note1X = Random.nextInt(20, 150).dp
    val note1Y = Random.nextInt(60, 200).dp
    val note2X = Random.nextInt(180, 280).dp
    val note2Y = Random.nextInt(220, 380).dp

    // 팝업 표시 상태 변수 유지
    var showPopup by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Spacer 100
        Spacer(modifier = Modifier.height(100.dp))

        // 200*25 primary color 긴 pill 안에 row
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(25.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "새로운 편지가 도착했어요",
                    fontFamily = pretendard,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                // 빨간 원형 안의 숫자
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2", // 예시 알림 수
                        fontFamily = pretendard,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Spacer 25
        Spacer(modifier = Modifier.height(25.dp))

        // 쪽지 팝업 2개 (랜덤 위치 예시)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        ) {
            // 쪽지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                // 쪽지 1
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 80.dp)
                        .offset(x = note1X, y = note1Y)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💌 편지 A",
                        fontFamily = pretendard,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }

                // 쪽지 2 (클릭 가능하도록 수정)
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 70.dp)
                        .offset(x = note2X, y = note2Y)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { showPopup = true }, // 👈 클릭 시 팝업 상태 true
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💬 편지 B",
                        fontFamily = pretendard,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }

        // Spacer 20
        Spacer(Modifier.weight(1f))

        // 300*50 primary color 긴 pill 버튼
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "편지 쓰러가기",
                fontFamily = pretendard,
                fontSize = 16.sp,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(56.dp))


        // ❌ Spacer 56 제거 (하단 여백은 MainScreenHost의 innerPadding으로 처리됨)
        // ❌ HomeBottomNavigation 호출 제거 (MainScreenHost에서 처리됨)
    }

    // 팝업 로직 추가: showPopup이 true일 때만 Dialog 표시
    if (showPopup) {
        Dialog(onDismissRequest = { showPopup = false }) {
            CustomNotePopup(
                onClose = { showPopup = false },
                noteContent = "편지 B의 내용입니다."
            )
        }
    }
}

@Composable
fun CustomNotePopup(
    onClose: () -> Unit,
    noteContent: String
) {
    val pretendard = FontFamily.Default

    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💬 편지 B",
                fontFamily = pretendard,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = noteContent,
                fontFamily = pretendard,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = "닫기",
                    fontFamily = pretendard,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)

@Composable

private fun HomeScreenPreview() {

    MaterialTheme {

        HomeScreen()

    }

}