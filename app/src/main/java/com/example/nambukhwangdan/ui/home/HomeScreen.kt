package com.example.nambukhwangdan.ui.home

import android.R.attr.fontFamily
import android.R.attr.fontWeight
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.google.common.io.Files.append

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
    val scope = rememberCoroutineScope()

    val lettersToProcess by viewModel.allLetters.collectAsState(initial = emptyList())
    val tomorrowLettersToProcess by diaryViewModel.receivedTomorrowLetters.collectAsState(initial = emptyList())
    val hasFutureLetter by diaryViewModel.hasFutureTomorrowLetter.collectAsState(initial = false)

    val actionableItems = remember(lettersToProcess, tomorrowLettersToProcess, hasFutureLetter) {
        val letterList = lettersToProcess.map { ActionableItem.ActionableLetter(it) }

        val tomorrowList = tomorrowLettersToProcess.map { ActionableItem.ActionableTomorrowLetter(it) }

        val dummyList = if (tomorrowList.isEmpty() && !hasFutureLetter) {
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
    letter: TomorrowLetter, // ⭐️ TomorrowLetter 타입 사용 (기능 유지)
    onClose: () -> Unit,
    bottomNavController: NavController,
    diaryViewModel: DiaryViewModel // ⭐️ DiaryViewModel 사용 (기능 유지)
) {
    val pretendard = FontFamily.Default
    val backgroundImageResId = R.drawable.popupbg
    val scaleFactor = 1.4f

    // 제목 로직 (기능 유지)
    val isDummy = letter.id == TomorrowLetter.FIRST_DIARY_DUMMY_ID
    val titleTextRaw = if (isDummy) {
        "첫 일기를 작성해 볼까요?"
    } else {
        "${formatDate(letter.deliveryTimestamp)}의 나에게서 온 편지"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(1f) // ⭐️ OtherLetterPopup과 동일한 원래 크기(0.85f) 복원
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        // 2. 배경 이미지 (1.4배 확대 적용)
        Image(
            painter = painterResource(id = backgroundImageResId),
            contentDescription = "팝업 배경",
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(
                    scaleX = scaleFactor,
                    scaleY = scaleFactor
                ),
            contentScale = ContentScale.Crop
        )

        // 3. 기존 컨텐츠 (컬럼)
        Column(
            modifier = Modifier
                .padding(28.dp) // 내부 패딩
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ⭐️ UI 통일: AnnotatedString 사용한 제목 스타일링 ⭐️
            Text(
                text = if (isDummy) {
                    buildAnnotatedString { append(titleTextRaw) }
                } else {
                    buildAnnotatedString {
                        // 날짜 부분에 Primary 색상 적용
                        withStyle(style = SpanStyle(color = Primary)) {
                            append(formatDate(letter.deliveryTimestamp))
                        }
                        append("의")
                        append("\n")
                        append(" 나에게서 온 편지")
                    }
                },
                fontFamily = pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 내용 (기능/스타일 유지)
            Text(
                text = letter.content,
                fontFamily = pretendard,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 4. 오늘의 일기 쓰기 버튼 (기능 유지, UI 통일)
            Button(
                onClick = {
                    bottomNavController.navigate(Routes.DiaryWrite) // ⭐️ 기능 유지
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    // ⭐️ OtherLetterPopup과 동일한 height (40.dp)
                    .height(40.dp),
                // ⭐️ UI 통일: Radius 99.dp 적용
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    "오늘의 일기 쓰기", // ⭐️ 텍스트 유지
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
    letter: Letter,
    onClose: () -> Unit,
    bottomNavController: NavController,
    viewModel: LetterViewModel
) {
    val pretendard = FontFamily.Default
    val backgroundImageResId = R.drawable.popupbg

    val scaleFactor = 1.4f

    // 1. Box를 사용하여 배경 이미지 위에 컨텐츠를 쌓고, 원래 팝업 크기를 유지합니다.
    Box(
        modifier = Modifier
            .fillMaxWidth(1f)
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp)), // Box 자체에 둥근 모서리를 적용하여 이미지와 내용이 밖으로 나가지 않도록 함
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = backgroundImageResId),
            contentDescription = "팝업 배경",
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(
                    scaleX = scaleFactor,
                    scaleY = scaleFactor
                ),
            contentScale = ContentScale.Crop // 팝업 영역에 맞춰 중앙 부분을 잘라 표시 (Crop이 가장 자연스러움)
        )

        // 3. 기존 컨텐츠 (컬럼)
        Column(
            modifier = Modifier
                .padding(28.dp) // 내부 패딩
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Primary)) {
                        append(formatDate(letter.createdAt))
                    }
                    append("에")
                    append("\n")
                    withStyle(style = SpanStyle(color = Primary)) {
                        append(letter.nickname)
                    }
                    append("님에게서 온 편지")
                },
                fontFamily = pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 내용
            Text(
                text = letter.content,
                fontFamily = pretendard,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 4. 답장하기 버튼 (Radius 99.dp 적용)
            Button(
                onClick = {
                    // ViewModel에서 필요한 데이터 설정 (예시)
                    // viewModel.setReplyToLetter(letter)
                    bottomNavController.navigate(Routes.ReplyScreen)
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    "답장하기",
                    fontFamily = pretendard,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            // 5. 닫기 버튼은 제거됨
        }
    }
}