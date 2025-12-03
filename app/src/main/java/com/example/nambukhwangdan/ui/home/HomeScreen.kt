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
import com.example.nambukhwangdan.model.Diary
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

    // ⭐️ sendToFuture=true 인 편지 목록을 사용합니다.
    val lettersToReply by viewModel.allSentLetters.collectAsState(initial = emptyList())
    val unrepliedCount = lettersToReply.size

    // 팝업 표시 상태 변수 유지
    var showPopup by remember { mutableStateOf(false) }
    var selectedLetter by remember { mutableStateOf<Diary?>(null) }

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
                            selectedLetter = letter
                            showPopup = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.paper3), // ⭐️ 이미지 리소스 사용
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
                    text = "편지 쓰러가기",
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
            CustomNotePopup(
                diary = selectedLetter!!,
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
            text = diary.content,
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