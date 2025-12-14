package com.example.nambukhwangdan.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.nambukhwangdan.R
import com.example.nambukhwangdan.model.Letter.Letter
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetter
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.screens.diary.formatDate
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.viewmodel.LetterViewModel
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import kotlin.random.Random
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

// ⭐️ 클릭된 아이템의 타입을 구분하기 위한 Wrapper 클래스 (모델 통합)
sealed class ActionableItem {
    data class ActionableLetter(val letter: Letter) : ActionableItem() // 유리병 편지 (Letter 플로우)
    data class ActionableTomorrowLetter(val letter: TomorrowLetter) : ActionableItem() // 미래 편지 (일기 플로우)
}

// ⭐️ 겹침 검사를 위한 Rect 클래스 정의
data class Rect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun overlaps(other: Rect): Boolean {
        // 두 사각형이 겹치는지 확인하는 로직
        return left < other.right && right > other.left &&
                top < other.bottom && bottom > other.top
    }
}

// 편지의 크기와 랜덤 범위 정의
private val NOTE_WIDTH = 100.dp
private val NOTE_HEIGHT = 70.dp
// ⭐️ 최대 범위는 편지 크기만큼 줄여서, 아이콘이 화면 밖으로 나가지 않도록 조정
private const val MAX_X = 280
private const val MIN_X = 20
private const val MAX_Y = 380
private const val MIN_Y = 60
private const val BOUNDARY_HEIGHT = 500 // 편지들이 생성될 영역의 최대 높이

@Composable
fun HomeScreen(
    viewModel: LetterViewModel,
    diaryViewModel: DiaryViewModel,
    bottomNavController: NavController
) {
    val pretendard = FontFamily.Default

    val lettersToProcess by viewModel.allLetters.collectAsState(initial = emptyList())
    val tomorrowLettersToProcess by diaryViewModel.receivedTomorrowLetters.collectAsState(initial = emptyList())

    val actionableItems = remember(lettersToProcess, tomorrowLettersToProcess) {
        val letterList = lettersToProcess.map { ActionableItem.ActionableLetter(it) }

        val tomorrowList = tomorrowLettersToProcess.map { ActionableItem.ActionableTomorrowLetter(it) }

        val dummyList = if (tomorrowList.isEmpty()) {
            val dummyLetter = TomorrowLetter.createDummy(userId = "")
            listOf(ActionableItem.ActionableTomorrowLetter(dummyLetter))
        } else {
            emptyList()
        }

        letterList + tomorrowList + dummyList
    }

    val unrepliedCount = actionableItems.size

    var showPopup by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ActionableItem?>(null) }


    val randomPositions = remember(actionableItems) {
        val positions = mutableMapOf<Any, Triple<Dp, Dp, Float>>()
        val occupiedRects = mutableListOf<Rect>()

        actionableItems.forEach { item ->
            val id = when(item) {
                is ActionableItem.ActionableLetter -> item.letter.id
                is ActionableItem.ActionableTomorrowLetter -> item.letter.id
            }

            var newX: Int
            var newY: Int
            var newRect: Rect
            var isOverlapping: Boolean

            do {
                newX = Random.nextInt(MIN_X, MAX_X - NOTE_WIDTH.value.toInt())
                newY = Random.nextInt(MIN_Y, MAX_Y - NOTE_HEIGHT.value.toInt())

                newRect = Rect(
                    left = newX.toFloat(),
                    top = newY.toFloat(),
                    right = (newX + NOTE_WIDTH.value).toFloat(),
                    bottom = (newY + NOTE_HEIGHT.value).toFloat()
                )

                isOverlapping = occupiedRects.any { existingRect ->
                    newRect.overlaps(existingRect)
                }
            } while (isOverlapping)

            val rotationAngle = Random.nextFloat() * (70.0f - (20.0f))

            positions[id] = Triple(newX.dp, newY.dp, rotationAngle)
            occupiedRects.add(newRect)
        }
        positions
    }

    // ⭐️ 1. 최상위 Box에 배경 이미지와 Column을 쌓습니다.
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.bg2),
            contentDescription = "배경 이미지",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Column (UI 요소들)
        Column(
            // ⭐️ 배경색 속성 제거
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))


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
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 10.sp,
                                color = Color.White,
                                //modifier = Modifier.offset(y = (-0.5).dp)
                            )
                        }
                    }
                }
            } else {
                // 편지가 없을 때 공백
                Spacer(modifier = Modifier.height(25.dp))
            }
            Spacer(modifier = Modifier.height(15.dp))


            val logoPainter = painterResource(id = R.drawable.titlewhite)
            Image(
                painter = logoPainter,
                contentDescription = "앱 로고",
                modifier = Modifier
                    .size(width = 250.dp, height = 48.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // ⭐️ 통합 목록을 순회하며 편지 아이콘을 생성
                actionableItems.forEach { item ->
                    // 아이템의 고유 ID를 가져와서 위치를 찾음
                    val id = when(item) {
                        is ActionableItem.ActionableLetter -> item.letter.id
                        is ActionableItem.ActionableTomorrowLetter -> item.letter.id
                    }
                    val (x, y, rotation) = randomPositions[id] ?: Triple(0.dp, 0.dp, 0.0f)

                    val imageResourceId = when(item) {
                        is ActionableItem.ActionableLetter -> R.drawable.lettero
                        is ActionableItem.ActionableTomorrowLetter -> R.drawable.letters
                    }

                    // Box는 위치 오프셋만 담당
                    Box(
                        modifier = Modifier
                            .offset(x = x, y = y),
                        contentAlignment = Alignment.Center
                    ) {
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val imageAlpha = if (isPressed) 0.7f else 1.0f

                        // ⭐️ 분기된 이미지 리소스 ID를 사용하여 Image 로드
                        Image(
                            painter = painterResource(id = imageResourceId),
                            contentDescription = when(item) {
                                is ActionableItem.ActionableLetter -> "누군가가 보낸 편지"
                                is ActionableItem.ActionableTomorrowLetter -> "어제의 내가 보낸 편지"
                            },
                            modifier = Modifier
                                .size(width = NOTE_WIDTH, height = NOTE_HEIGHT)
                                .rotate(rotation)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = {
                                        // 클릭된 item을 선택
                                        selectedItem = item
                                        showPopup = true
                                    }
                                ),
                            contentScale = ContentScale.Fit,
                            alpha = imageAlpha
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

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
                        text = "유리병 편지 쓰기",
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
                        diaryViewModel.setReplyingToTomorrowLetterId(item.letter.id)

                        SelfLetterPopup(
                            letter = item.letter,
                            onClose = { showPopup = false },
                            bottomNavController = bottomNavController,
                            diaryViewModel = diaryViewModel
                        )
                    }
                    is ActionableItem.ActionableLetter -> {
                        viewModel.setReplyToId(item.letter.id)

                        OtherLetterPopup(
                            letter = item.letter,
                            onClose = { showPopup = false },
                            bottomNavController = bottomNavController,
                            viewModel = viewModel
                        )
                    }
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
            val titleText = if (letter.id == TomorrowLetter.FIRST_DIARY_DUMMY_ID) {
                "첫 일기를 작성해 볼까요?"
            } else {
                "${formatDate(letter.deliveryTimestamp)}의 나에게서 온 편지"
            }
            // ⭐️ 제목: TomorrowLetter의 전달 날짜 사용
            Text(
                text = titleText,
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
                    "오늘의 일기 쓰기",
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
                text = "${formatDate(letter.createdAt)}에 ${letter.nickname}님에게서 온 편지",
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
                    "답장하기",
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