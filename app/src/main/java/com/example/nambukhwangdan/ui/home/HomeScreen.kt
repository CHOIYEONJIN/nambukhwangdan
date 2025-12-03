package com.example.nambukhwangdan.ui.home

import androidx.compose.foundation.Image
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.nambukhwangdan.R // R.drawable.my_letter_icon을 위해 필요
import com.example.nambukhwangdan.model.Diary.Diary
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.screens.diary.formatDate // formatDate 함수가 해당 패키지에 있다고 가정
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import kotlin.random.Random

// ⭐️ 편지의 크기와 랜덤 범위 정의
private val NOTE_WIDTH = 100.dp
private val NOTE_HEIGHT = 70.dp
private const val MAX_X = 280
private const val MIN_X = 20
private const val MAX_Y = 380
private const val MIN_Y = 60
private const val BOUNDARY_HEIGHT = 500 // 편지들이 생성될 영역의 최대 높이

@Composable
fun HomeScreen( viewModel: DiaryViewModel,
                bottomNavController: NavController) {
    val pretendard = FontFamily.Default

    // ⭐️ DiaryViewModel.allSentLetters는 현재 '답장해야 할 모든 편지' 데이터를 가져온다고 가정합니다.
    val lettersToReply by viewModel.allSentLetters.collectAsState(initial = emptyList())
    val unrepliedCount = lettersToReply.size

    // 팝업 표시 상태 변수 유지
    var showPopup by remember { mutableStateOf(false) }
    var selectedLetter by remember { mutableStateOf<Diary?>(null) } // Diary 타입으로 유지

    // ⭐️ 각 편지의 랜덤 위치를 저장하는 맵
    val randomPositions = remember(lettersToReply) {
        lettersToReply.associate { letter ->
            val x = Random.nextInt(MIN_X, MAX_X).dp
            val y = Random.nextInt(MIN_Y, MAX_Y).dp
            letter.id to Pair(x, y)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ⭐️ 알림 바: 편지 수에 따라 표시
        if (unrepliedCount > 0) {
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
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unrepliedCount.toString(), // ⭐️ 실제 편지 수
                            fontFamily = pretendard,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // 편지가 없을 때 공백
            Spacer(modifier = Modifier.height(25.dp))
        }

        // Spacer 25
        Spacer(modifier = Modifier.height(25.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BOUNDARY_HEIGHT.dp)
        ) {
            // ⭐️ lettersToReply 목록을 순회하며 편지 아이콘을 생성
            lettersToReply.forEach { letter ->
                val (x, y) = randomPositions[letter.id] ?: (0.dp to 0.dp) // 랜덤 위치 사용

                Box(
                    modifier = Modifier
                        .size(width = NOTE_WIDTH, height = NOTE_HEIGHT)
                        .offset(x = x, y = y)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            // ⭐️ 수정: 클릭된 letter를 선택
                            selectedLetter = letter
                            showPopup = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // R.drawable.paper3 리소스를 사용
                    Image(
                        painter = painterResource(id = R.drawable.paper3),
                        contentDescription = "답장할 편지",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .width(300.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {bottomNavController.navigate(Routes.NewLetter)},
                colors= ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)){
                Text(
                    // ⭐️ 수정: 버튼 텍스트를 "내일의 나에게 편지 쓰러가기"로 변경
                    text = "미래의 나에게 편지 쓰러가기", // 텍스트를 조금 더 일반적인 '미래'로 변경
                    fontFamily = pretendard,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(56.dp))
    }

    if (showPopup && selectedLetter != null) {
        Dialog(onDismissRequest = { showPopup = false }) {
            // ⭐️ 분기 로직: emotion 필드를 사용하여 두 가지 팝업 중 하나를 선택
            val diary = selectedLetter!!
            // 임시 구분: emotion이 비어있으면 남에게서 온 편지(답장 필요), 아니면 나에게서 온 편지(일기 쓰기)로 가정
            val isOtherLetter = diary.emotion.isNullOrEmpty()

            if (isOtherLetter) {
                OtherLetterPopup(
                    diary = diary,
                    onClose = { showPopup = false },
                    bottomNavController = bottomNavController,
                    viewModel = viewModel
                )
            } else {
                SelfLetterPopup(
                    diary = diary,
                    onClose = { showPopup = false },
                    bottomNavController = bottomNavController,
                    viewModel = viewModel
                )
            }
        }
    }
}

// ====================================================================
// 1. 나에게서 온 편지 (일기 쓰기로 이동) 팝업
// ====================================================================
@Composable
fun SelfLetterPopup(
    diary: Diary,
    onClose: () -> Unit,
    bottomNavController: NavController,
    viewModel: DiaryViewModel
) {
    val pretendard = FontFamily.Default

    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    )
    {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ⭐️ 제목: 나에게서 온 편지
            Text(
                text = "💌 ${formatDate(diary.createdAt)}의 나에게서 온 편지",
                fontFamily = pretendard, fontSize = 20.sp, color = Color.Black, modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = diary.content,
                fontFamily = pretendard,
                fontSize = 16.sp, color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Button(
                onClick = {
                    // 1. 답장 대상 ID 설정
                    viewModel.setReplyToId(diary.id)
                    // 2. 오늘의 일기(DiaryWrite) 화면으로 이동
                    bottomNavController.navigate(Routes.DiaryWrite)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    "오늘의 일기 쓰기 (자신에게 답장)", // ⭐️ 텍스트 변경
                    fontFamily = pretendard,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
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

// ====================================================================
// 2. 남에게서 온 편지 (다른 사용자에게 답장) 팝업
// ====================================================================
@Composable
fun OtherLetterPopup(
    diary: Diary,
    onClose: () -> Unit,
    bottomNavController: NavController,
    viewModel: DiaryViewModel
) {
    val pretendard = FontFamily.Default
    // ⭐️ 임시 닉네임 설정 (실제로는 Diary 모델에 senderNickname 필드가 있어야 함)
    val senderNickname = "누군가"

    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    )
    {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ⭐️ 제목: 남에게서 온 편지
            Text(
                text = "💌 ${formatDate(diary.createdAt)}에 ${senderNickname}님에게서 온 편지",
                fontFamily = pretendard, fontSize = 20.sp, color = Color.Black, modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = diary.content,
                fontFamily = pretendard,
                fontSize = 16.sp, color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Button(
                onClick = {
                    // 1. 답장 대상 ID 설정
                    viewModel.setReplyToId(diary.id)
                    // 2. 다른 사용자에게 답장하는 전용 화면으로 이동
                    bottomNavController.navigate(Routes.ReplyScreen)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    "답장하기 (다른 사용자에게 답장)", // ⭐️ 텍스트 변경
                    fontFamily = pretendard,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
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