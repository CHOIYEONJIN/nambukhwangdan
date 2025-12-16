package com.example.nambukhwangdan.screens.diary

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.ui.theme.Variables
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.nambukhwangdan.data.detailStickerMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeResultScreen(
    viewModel: DiaryViewModel,
    bottomNavController: NavController

) {
    val emotionColors = mapOf(
        "긍정" to Color(0xFFF8D671),   // 초록 (긍정)
        "중립" to Color(0xFFD9C4A9),   // 노랑 (중립)
        "부정" to Color(0xFF6194D7)    // 빨강 (부정)
    )

    // Firestore에서 감정 결과 수신
    val detectedSentiment by viewModel.detectedSentiment.collectAsState()

    val diary by viewModel.todayDiary.collectAsState()
    val dateMillis by viewModel.selectedDateMillis.collectAsState()
    val todayMillis = System.currentTimeMillis()

    // ✅ Compose 내장 DatePicker 상태
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayMillis
    )
    val pickedMillis = datePickerState.selectedDateMillis ?: todayMillis
    val headlineStr = remember(pickedMillis) {
        SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
            .format(Date(pickedMillis))
    }

    // ✅ 달력 팝업 표시 여부
    var showCalendar by remember { mutableStateOf(false) }

    val dateStr = remember(dateMillis) {
        SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date(dateMillis))
    }
    val selectedEmotion by viewModel.selectedEmotion.collectAsState()
    val selectedSticker by viewModel.selectedSticker.collectAsState()
    val emotionCats = listOf("긍정", "중립", "부정")


    val safeEmotion = selectedEmotion ?: detectedSentiment ?: "기쁨"

    val detailStickers = remember(safeEmotion) {
        when (safeEmotion) {
            "긍정" -> listOf("p1", "p2", "p3", "p4", "p5")
            "중립" -> listOf("a1", "a2", "a3", "a4", "a5")
            "부정" -> listOf("n1", "n2", "n3", "n4", "n5")
            else -> listOf("n2", "a2", "p1", "a4", "n3") // 최소 Fallback
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Variables.Color4)
    ) {
        // 🔹 메인 컨텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 날짜 + indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dateStr)
                    IconButton(onClick = { showCalendar = true }) { // ✅ 팝업 열기
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "날짜 선택"
                        )
                    }
                }

                // 인디케이터
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        Modifier
                            .padding(5.dp)
                            .size(10.dp)
                            .background(color = Color.White, shape = CircleShape)
                    )
                    Box(
                        Modifier
                            .padding(5.dp)
                            .width(24.dp)
                            .height(10.dp)
                            .background(
                                color = Primary,
                                shape = RoundedCornerShape(999.dp)
                            )
                    )
                    Box(
                        Modifier
                            .padding(5.dp)
                            .size(10.dp)
                            .background(color = Color.White, shape = CircleShape)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ⭐️ 수정된 부분: 단일 item으로 오늘 일기 미리보기를 표시합니다.
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 30.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                            .background(Surface, RoundedCornerShape(10.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //오늘 일기 text 부분
                        Text("오늘의 일기 미리보기", fontWeight = FontWeight.Bold)
                        Text(diary, Modifier.padding(10.dp), fontSize = 14.sp)
                    }
                }

                // 감정 목록
                item {
                    Spacer(Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 30.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                            .background(Surface, RoundedCornerShape(10.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LaunchedEffect(detectedSentiment) {
                            // detectedSentiment가 null이 아니고, selectedEmotion이 아직 선택되지 않았을 때
                            if (detectedSentiment != null && selectedEmotion == null) {
                                // detectedSentiment는 String? 타입이지만, if문 내부에서는 String 타입이 보장됨
                                viewModel.chooseEmotion(detectedSentiment!!)
                            }
                        }
                        Text(
                            // null 체크를 통해 "분석 중..." 또는 감정 표시
                            text = "오늘의 감정은: ${selectedEmotion ?: detectedSentiment ?: "분석 중..."}",
                            fontWeight = FontWeight.Medium
                        )

                        Text("감정 분류 선택", fontWeight = FontWeight.Bold)
                        LazyRow(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            items(emotionCats.size) { i ->
                                val label = emotionCats[i]
                                // detectedSentiment와 selectedEmotion 중 하나를 기준으로 선택 상태 결정
                                val isSelected = (selectedEmotion ?: detectedSentiment) == label
                                val bgColor = emotionColors[label] ?: Color.LightGray

                                EmotionCircleButton(
                                    label = label,
                                    isSelected = isSelected,
                                    bgColor = bgColor,
                                    onClick = { viewModel.chooseEmotion(label) }
                                )
                            }
                        }



                        Text("스티커로 다이어리를 꾸며주세요!", fontWeight = FontWeight.Bold)

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),     // 한 줄에 5개씩
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .heightIn(min = 80.dp, max = 200.dp),   // 안정적인 높이 확보!
                            horizontalArrangement = Arrangement.Center
                        ) {
                            items(detailStickers) { stickerName ->
                                val isSelected = selectedSticker == stickerName

                                // ⭐️⭐️ 상세 감정 스티커를 이미지로 표시하도록 수정 ⭐️⭐️
                                val imageResId = detailStickerMap[stickerName]

                                EmotionCircleButton(
                                    label = stickerName, // Content Description용으로 사용
                                    isSelected = isSelected,
                                    bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, // 이미지 사용 시 투명 또는 원하는 색상
                                    imageRes = imageResId, // 이미지 리소스 ID 전달
                                    onClick = { viewModel.chooseSticker(stickerName) }
                                )
                            }
                        }


                    }
                }
            }
        }
        // 하단 버튼
        Button(
            onClick = {
                viewModel.updateDiaryWithAnalysisResult()
                viewModel.resetDetectedSentiment() // 다음 화면으로 넘어가기 전 분석 결과 초기화 (선택적)
                bottomNavController.navigate(Routes.LetterToTomorrow)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text(
                "다음으로",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
        if (showCalendar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),// 기존 화면에 투명도 50의 검은 색 레이어를 씌움
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor =Surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "어떤 날의 기록인가요?",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        Spacer(Modifier.height(12.dp))

                        DatePicker(
                            state = datePickerState,
                            title = null,
                            headline = {                   // 선택한 날짜 크게 보여주는 부분
                                Text(
                                    text = headlineStr,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                            },
                            showModeToggle = false,
                            colors = DatePickerDefaults.colors(
                                containerColor = Surface,
                                titleContentColor = Variables.Color4,
                                weekdayContentColor = Color.Black,
                                selectedDayContainerColor = Primary,
                                selectedDayContentColor = Color.White,
                                todayContentColor = Primary
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showCalendar = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) { Text("취소", color = Color.White) }

                            Button(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let {
                                        viewModel.setSelectedDate(it)
                                    }
                                    showCalendar = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) { Text("확인", color = Color.White) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmotionCircleButton(
    label: String,
    isSelected: Boolean,
    bgColor: Color,
    imageRes: Int? = null,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        label = ""
    )

    Box(
        modifier = Modifier
            .padding(6.dp)
            .size(45.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                if (imageRes == null) {
                    // 감정 분류(긍정/중립/부정) 버튼 (이미지 미사용)
                    bgColor.copy(alpha = if (isSelected) 1f else 0.4f)
                } else {
                    // 상세 감정 스티커 (이미지 사용) - 배경은 투명하게 하고 선택 시 크기 변화만 줍니다.
                    // 만약 이미지 뒤에 선택된 배경색을 원하면 여기서 bgColor를 사용하도록 수정 가능합니다.
                    Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageRes == null) {
            // 텍스트 버튼 (긍정/중립/부정)
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (isSelected) Color.White else Color.Black,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        } else {
            // 이미지 스티커 버튼
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label,
                // 스티커 크기를 조정할 수 있습니다.
                modifier = Modifier.size(50.dp).clip(CircleShape)
            )
        }
    }
}