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
import com.example.nambukhwangdan.R
import com.example.nambukhwangdan.model.Letter.Letter
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetter // ⭐️ TomorrowLetter 모델 임포트
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.screens.diary.formatDate
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.viewmodel.LetterViewModel
import com.example.nambukhwangdan.viewmodel.DiaryViewModel // ⭐️ DiaryViewModel 임포트
import kotlin.random.Random

// ⭐️ 클릭된 아이템의 타입을 구분하기 위한 Wrapper 클래스 (모델 통합)
sealed class ActionableItem {
    data class ActionableLetter(val letter: Letter) : ActionableItem() // 유리병 편지 (Letter 플로우)
    data class ActionableTomorrowLetter(val letter: TomorrowLetter) : ActionableItem() // 미래 편지 (일기 플로우)
}

// 편지의 크기와 랜덤 범위 정의
private val NOTE_WIDTH = 100.dp
private val NOTE_HEIGHT = 70.dp
private const val MAX_X = 280
private const val MIN_X = 20
private const val MAX_Y = 380
private const val MIN_Y = 60
private const val BOUNDARY_HEIGHT = 500 // 편지들이 생성될 영역의 최대 높이

@Composable
fun HomeScreen(
    viewModel: LetterViewModel,
    // ⭐️ DiaryViewModel 추가
    diaryViewModel: DiaryViewModel,
    bottomNavController: NavController
) {
    val pretendard = FontFamily.Default

    // ⭐️ 1. LetterViewModel의 모든 편지 (익명/답장)
    val lettersToProcess by viewModel.allLetters.collectAsState(initial = emptyList())

    // ⭐️ 2. DiaryViewModel의 도착한 TomorrowLetter (일기 작성 유도)
    val tomorrowLettersToProcess by diaryViewModel.receivedTomorrowLetters.collectAsState(initial = emptyList())

    // ⭐️ 3. 두 목록을 합쳐서 홈 화면에 표시할 아이템 목록 생성
    val actionableItems = remember(lettersToProcess, tomorrowLettersToProcess) {
        val letterList = lettersToProcess.map { ActionableItem.ActionableLetter(it) }
        val tomorrowList = tomorrowLettersToProcess.map { ActionableItem.ActionableTomorrowLetter(it) }
        letterList + tomorrowList
    }

    val unrepliedCount = actionableItems.size

    // 팝업 표시 상태 변수 유지
    var showPopup by remember { mutableStateOf(false) }
    // ⭐️ 클릭된 아이템은 통합된 ActionableItem 타입으로 저장
    var selectedItem by remember { mutableStateOf<ActionableItem?>(null) }


    // ⭐️ 각 아이템의 랜덤 위치를 저장하는 맵
    val randomPositions = remember(actionableItems) {
        actionableItems.associate { item ->
            // 고유 ID를 키로 사용
            val id = when(item) {
                is ActionableItem.ActionableLetter -> item.letter.id
                is ActionableItem.ActionableTomorrowLetter -> item.letter.id
            }
            val x = Random.nextInt(MIN_X, MAX_X).dp
            val y = Random.nextInt(MIN_Y, MAX_Y).dp
            id to Pair(x, y)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ⭐️ 알림 바: 총 아이템 수에 따라 표시
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
                            text = unrepliedCount.toString(), // ⭐️ 총 아이템 수
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
            // ⭐️ 통합 목록을 순회하며 편지 아이콘을 생성
            actionableItems.forEach { item ->
                // 아이템의 고유 ID를 가져와서 위치를 찾음
                val id = when(item) {
                    is ActionableItem.ActionableLetter -> item.letter.id
                    is ActionableItem.ActionableTomorrowLetter -> item.letter.id
                }
                val (x, y) = randomPositions[id] ?: (0.dp to 0.dp)

                Box(
                    modifier = Modifier
                        .size(width = NOTE_WIDTH, height = NOTE_HEIGHT)
                        .offset(x = x, y = y)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            // 클릭된 item을 선택
                            selectedItem = item
                            showPopup = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.paper3),
                        contentDescription = "처리할 편지",
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
                .background(Color.Transparent), // 배경을 투명하게 하거나 Primary로 설정
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    // 무조건 DiaryWrite로 이동
                    bottomNavController.navigate(Routes.DiaryWrite)
                },
                // Primary 색상 사용
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "일기 쓰기 시작 (테스트)",
                    fontFamily = pretendard,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp)) // 두 버튼 사이에 간격 추가



        Box(
            modifier = Modifier
                .width(300.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { bottomNavController.navigate(Routes.NewLetter) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
            ) {
                Text(
                    text = "미래의 나에게 편지 쓰러가기",
                    fontFamily = pretendard,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(56.dp))
    }

    if (showPopup && selectedItem != null) {
        Dialog(onDismissRequest = { showPopup = false }) {
            // ⭐️ 팝업 분기 로직: ActionableItem의 실제 타입에 따라 분기
            when (val item = selectedItem!!) {
                is ActionableItem.ActionableTomorrowLetter -> {
                    // 1. TomorrowLetter (일기 플로우): SelfLetterPopup으로 연결
                    // ⭐️ DiaryViewModel에 응답할 TomorrowLetter ID 설정
                    diaryViewModel.setReplyingToTomorrowLetterId(item.letter.id)

                    SelfLetterPopup(
                        letter = item.letter, // TomorrowLetter 모델 전달
                        onClose = { showPopup = false },
                        bottomNavController = bottomNavController,
                        diaryViewModel = diaryViewModel // DiaryViewModel 전달
                    )
                }
                is ActionableItem.ActionableLetter -> {
                    // 2. Letter (유리병 편지 플로우): OtherLetterPopup으로 연결
                    // ⭐️ LetterViewModel에 응답할 Letter ID 설정
                    viewModel.setReplyToId(item.letter.id)

                    OtherLetterPopup(
                        letter = item.letter, // Letter 모델 전달
                        onClose = { showPopup = false },
                        bottomNavController = bottomNavController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

}

// ====================================================================
// 1. 나에게서 온 편지 (미래의 나에게 보낸 편지) 팝업 (TomorrowLetter 처리 -> 일기 작성)
// ====================================================================
@Composable
fun SelfLetterPopup(
    letter: TomorrowLetter, // ⭐️ TomorrowLetter 타입 사용
    onClose: () -> Unit,
    bottomNavController: NavController,
    diaryViewModel: DiaryViewModel // ⭐️ DiaryViewModel 사용
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
            // ⭐️ 제목: TomorrowLetter의 전달 날짜 사용
            Text(
                text = "💌 ${formatDate(letter.deliveryTimestamp)}의 나에게서 온 편지",
                fontFamily = pretendard, fontSize = 20.sp, color = Color.Black, modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = letter.content,
                fontFamily = pretendard,
                fontSize = 16.sp, color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Button(
                onClick = {
                    // 1. 이미 Home에서 TomorrowLetter ID가 설정됨
                    // 2. 오늘의 일기(DiaryWrite) 화면으로 이동
                    bottomNavController.navigate(Routes.DiaryWrite)
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    "오늘의 일기 쓰기 (이 편지에 대한 응답)",
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
// 2. 남에게서 온 편지 (다른 사용자에게 답장) 팝업 (Letter 처리 -> 답장 작성)
// ====================================================================
@Composable
fun OtherLetterPopup(
    letter: Letter, // Letter 타입 사용 (기존 유지)
    onClose: () -> Unit,
    bottomNavController: NavController,
    viewModel: LetterViewModel // LetterViewModel 사용 (기존 유지)
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
            // 제목: 남에게서 온 편지 (닉네임 사용)
            Text(
                text = "💌 ${formatDate(letter.createdAt)}에 ${letter.nickname}님에게서 온 편지",
                fontFamily = pretendard, fontSize = 20.sp, color = Color.Black, modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = letter.content,
                fontFamily = pretendard,
                fontSize = 16.sp, color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Button(
                onClick = {
                    // 1. 이미 Home에서 Letter ID가 설정됨
                    // 2. 다른 사용자에게 답장하는 전용 화면으로 이동
                    bottomNavController.navigate(Routes.ReplyScreen)
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    "답장하기 (다른 사용자에게 답장)",
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