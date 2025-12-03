package com.example.nambukhwangdan.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.nambukhwangdan.model.Diary.Diary
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.screens.diary.formatDate
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import kotlin.random.Random

@Composable
fun HomeScreen( viewModel: DiaryViewModel,
                bottomNavController: NavController) {
    val pretendard = FontFamily.Default
    val note1X = Random.nextInt(20, 150).dp
    val note1Y = Random.nextInt(60, 200).dp
    val note2X = Random.nextInt(180, 280).dp
    val note2Y = Random.nextInt(220, 380).dp

    // 팝업 표시 상태 변수 유지
    var showPopup by remember { mutableStateOf(false) }
    var selectedDiary by remember { mutableStateOf<Diary?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                        .clickable {
                            // 예: pastLetters 중 원하는 편지를 선택 (지금은 index 0 예시)
                            selectedDiary = viewModel.pastLetters.value[0]
                            showPopup = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("💬 편지 B",
                        fontFamily = pretendard,
                        fontSize = 14.sp,
                        color = Color.Black)
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
            Button(onClick = {bottomNavController.navigate(Routes.NewLetter)}, colors= ButtonColors(Primary,Color.White,
                Background,Color.White)){
            Text(
                text = "편지 쓰러가기",
                fontFamily = pretendard,
                fontSize = 16.sp,
                color = Color.White
            )}
        }
        Spacer(modifier = Modifier.height(56.dp))


        // ❌ Spacer 56 제거 (하단 여백은 MainScreenHost의 innerPadding으로 처리됨)
        // ❌ HomeBottomNavigation 호출 제거 (MainScreenHost에서 처리됨)
    }

    // 팝업 로직 추가: showPopup이 true일 때만 Dialog 표시
    if (showPopup && selectedDiary != null) {
        Dialog(onDismissRequest = { showPopup = false }) {
            CustomNotePopup(
                diary = selectedDiary!!,
                onClose = { showPopup = false },
                bottomNavController = bottomNavController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun CustomNotePopup(
    diary: Diary,
    onClose: () -> Unit,
    bottomNavController: NavController,
    viewModel: DiaryViewModel
)
{
    val pretendard = FontFamily.Default

    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    )
    { Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally){
            Text(
            text = "💌 ${formatDate(diary.createdAt)}의 ${diary.nickname}에게서 온 편지",
            fontFamily = pretendard, fontSize = 20.sp, color = Color.Black, modifier = Modifier.padding(bottom = 16.dp))

            Text(
                text = diary.content,  // ← 실제 내용 출력됨!
                fontFamily = pretendard,
                fontSize = 16.sp, color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp))
            Button(
                onClick = {
                    viewModel.setReplyToId(diary.id)
                    bottomNavController.navigate(Routes.DiaryWrite)
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.
                buttonColors(containerColor = Primary)
            ) {
                Text("답장하기",
                    fontFamily = pretendard,
                    fontSize = 18.sp,
                    color = Color.White)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button( onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.
            buttonColors(containerColor = Primary) ) {
            Text( text = "닫기",
                fontFamily = pretendard,
                fontSize = 18.sp,
                color = Color.White )
            }
        }
    }
}


