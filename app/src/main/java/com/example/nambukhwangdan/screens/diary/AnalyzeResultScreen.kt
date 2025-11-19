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
import com.example.nambukhwangdan.ui.theme.Variables
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyzeResultScreen(
    viewModel: DiaryViewModel,
    bottomNavController: NavController

) {
    val emotionColors = mapOf(
        "긍정" to Color(0xFF8BC34A),   // 초록 (긍정)
        "중립" to Color(0xFFFFC107),   // 노랑 (중립)
        "부정" to Color(0xFFF44336)    // 빨강 (부정)
    )
    val pastLetters by viewModel.pastLetters.collectAsState()
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
    val detected by viewModel.detectedEmotion.collectAsState()
    val selectedEmotion by viewModel.selectedEmotion.collectAsState()
    val selectedSticker by viewModel.selectedSticker.collectAsState()
    val emotionCats = listOf("긍정", "중립", "부정")
    val safeEmotion = selectedEmotion ?: detected ?: "기쁨"  // 안전한 기본값!

    val detailStickers = remember(safeEmotion) {
        when (safeEmotion) {
            "긍정" -> listOf("기쁨", "감사", "뿌듯", "설렘", "안도")
            "중립" -> listOf("차분", "평온", "무던", "담담", "관망")
            "부정" -> listOf("분노", "슬픔", "피곤", "불안", "죄책")
            else -> listOf("기쁨", "감사", "분노", "놀람", "슬픔") // 최소 Fallback
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
                            .width(24.dp)
                            .height(10.dp)
                            .background(
                                color = Variables.Color5,
                                shape = RoundedCornerShape(999.dp)
                            )
                    )
                    Box(
                        Modifier
                            .padding(5.dp)
                            .size(10.dp)
                            .background(color = Color.White, shape = CircleShape)
                    )
                    Box(
                        Modifier
                            .padding(5.dp)
                            .size(10.dp)
                            .background(color = Color.White, shape = CircleShape)
                    )
                }
            }

            // 🔹 LazyColumn (내용 시작)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 오늘 일기 카드
                items(pastLetters.size) { index ->
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                            .background(Variables.Color6, RoundedCornerShape(10.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //오늘 일기 text 부분
                        Text("오늘의 일기 미리보기", fontWeight = FontWeight.Bold)
                        ExpandableDiaryCard(diary)
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
                                .background(Variables.Color6, RoundedCornerShape(10.dp))
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                        LaunchedEffect(Unit) {
                            if (selectedEmotion == null) {   // 이미 선택되어 있으면 덮어쓰지 않음
                                viewModel.chooseEmotion(detected ?: "분석중")
                            }
                        }
                        Text("오늘의 감정은?  ${selectedEmotion ?: detected}", fontWeight = FontWeight.Medium)

                        Text("감정 분류 선택", fontWeight = FontWeight.Bold)
                        LazyRow(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            items(emotionCats.size) { i ->
                                val label = emotionCats[i]
                                val isSelected = (selectedEmotion ?: detected) == label
                                val bgColor = emotionColors[label] ?: Color.LightGray

                                EmotionCircleButton(
                                    label = label,
                                    isSelected = isSelected,
                                    bgColor = bgColor,
                                    onClick = { viewModel.chooseEmotion(label) }
                                )
                            }
                        }



                        Text("상세 감정 스티커 선택", fontWeight = FontWeight.Bold)
                        Text("상세 감정 스티커 선택", fontWeight = FontWeight.Bold)

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),     // ← 한 줄에 5개!
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .wrapContentHeight(),          // 필요한 만큼만 세로 공간 사용
                            horizontalArrangement = Arrangement.Center
                        ) {
                            items(detailStickers) { sticker ->
                                val isSelected = selectedSticker == sticker

                                EmotionCircleButton(
                                    label = sticker,
                                    isSelected = isSelected,
                                    bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    onClick = { viewModel.chooseSticker(sticker) }
                                )
                            }
                        }


                    }
                }
            }
        }
        // 하단 버튼
        Button(
            onClick = { bottomNavController.navigate(Routes.Home)},
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Variables.Color5)
        ) {
            Text(
                "완료하기",
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
                    colors = CardDefaults.cardColors(containerColor = Variables.Color6),
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
                                containerColor = Variables.Color6,
                                titleContentColor = Variables.Color4,
                                weekdayContentColor = Color.Black,
                                selectedDayContainerColor = Variables.Color5,
                                selectedDayContentColor = Color.White,
                                todayContentColor = Variables.Color5
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
                                colors = ButtonDefaults.buttonColors(containerColor = Variables.Color5)
                            ) { Text("취소", color = Color.White) }

                            Button(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let {
                                        viewModel.setSelectedDate(it)
                                    }
                                    showCalendar = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Variables.Color5)
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
                    bgColor.copy(alpha = if (isSelected) 1f else 0.4f)
                } else {
                    Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageRes == null) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (isSelected) Color.White else Color.Black,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        } else {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label,
                modifier = Modifier.size(42.dp).clip(CircleShape)
            )
        }
    }
}
