package com.example.nambukhwangdan.screens.diary

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.screens.letters.ExpandableDiaryCard
import com.example.nambukhwangdan.screens.letters.formatDate
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.ui.theme.Variables
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryWriteScreen(
    viewModel: DiaryViewModel,
    bottomNavController: NavController
) {
    val selectedUris by viewModel.selectedUris.collectAsState()
    val pastLetters by viewModel.allDiaries.collectAsState() // allDiaries로 변경 (LetterViewModel이 아님)
    val diary by viewModel.todayDiary.collectAsState()
    val dateMillis by viewModel.selectedDateMillis.collectAsState()
    val todayMillis = System.currentTimeMillis()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState() // drawing의 collectAsState 사용

    // ✅ Compose 내장 DatePicker 상태
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayMillis
    )
    val pickedMillis = datePickerState.selectedDateMillis ?: todayMillis
    val headlineStr = remember(pickedMillis) {
        SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
            .format(Date(pickedMillis))
    }
    // ✅ 분석 결과 도착 시 화면 이동 (상단에 있던 기존 로직 유지)
    LaunchedEffect(viewModel.detectedSentiment.collectAsState().value) {
        if (viewModel.detectedSentiment.value != null) {
            bottomNavController.navigate(Routes.AnalyzeResult)
        }
    }
    // ✅ 달력 팝업 표시 여부
    var showCalendar by remember { mutableStateOf(false) }

    val dateStr = remember(dateMillis) {
        SimpleDateFormat("M월 d일 (E)", Locale.KOREA).format(Date(dateMillis))
    }

    //사진 선택을 위한 launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3),
        onResult = { uris ->
            val merged = (selectedUris + uris).distinct().take(3)  // 기존 + 신규 + 중복 제거
            viewModel.setSelectedUris(merged)
        }
    )
    LaunchedEffect(Unit) {
        viewModel.updateDiary("")   // 항상 입력 칸은 빈칸
    }
    val replyToId by viewModel.replyToId.collectAsState()

// pastLetters (allDiaries) 중 replyToId와 동일한 id를 가진 것만 필터링
    val replyLetter = pastLetters.find { it.id == replyToId }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Background)
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
                    Box(
                        Modifier
                            .padding(5.dp)
                            .size(10.dp)
                            .background(color = Color.White, shape = CircleShape)
                    )
                }
            }

            // 🔹 LazyColumn (본문)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 특정 편지 하나만 보여주기
                replyLetter?.let { letter ->
                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .shadow(4.dp, RoundedCornerShape(10.dp))
                                .fillMaxWidth()
                                .background(Surface, RoundedCornerShape(10.dp))
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // nickname은 Diary 모델에 추가되었으므로 사용 가능
                            Text(
                                "${formatDate(letter.createdAt)}의 ${letter.nickname}에게서 온 편지",
                                fontWeight = FontWeight.SemiBold
                            )
                            ExpandableDiaryCard(letter.content)
                        }
                    }
                }

                // 오늘의 일기 작성 박스
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

                        // 📌 1) 사진 / 아이콘을 담는 영역 (clickable 제거)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .height(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface),
                        ) {

                            if (selectedUris.isEmpty()) {
                                // 📌 2) 사진 없으면 아이콘만 → 이 영역이 클릭 가능
                                Box(
                                    modifier = Modifier
                                        .size(35.dp)
                                        .align(Alignment.Center)
                                        .clip(CircleShape)
                                        .background(Surface)
                                        .border(1.dp, Primary, CircleShape)
                                        .clickable {
                                            launcher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "사진 추가",
                                        tint = Primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                            } else {
                                // 📌 3) 사진 있을 때 → LazyRow (clickable ❌)
                                // 📌 LazyRow 안에 사진 + (+) 버튼 같이 넣기
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center                                  ) {
                                    // 1) 선택된 사진들 먼저 보여주기
                                    items(selectedUris.take(3)) { uri ->
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(6.dp)
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(uri),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(10.dp))
                                            )

                                            // 삭제 버튼
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "삭제",
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .align(Alignment.TopEnd)
                                                    .background(
                                                        Color.Black.copy(alpha = 0.4f),
                                                        CircleShape
                                                    )
                                                    .clip(CircleShape)
                                                    .clickable { viewModel.removeUri(uri) }
                                            )
                                        }
                                    }

                                    // 📌 2) 사진이 3장 미만일 때 → + 버튼을 LazyRow에 item으로 추가!
                                    if (selectedUris.size < 3) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp) // 사진 크기랑 동일하게 맞춤
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Surface)
                                                    .border(
                                                        1.dp,
                                                        Primary,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable {
                                                        launcher.launch(
                                                            PickVisualMediaRequest(
                                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                                            )
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "사진 추가",
                                                    tint = Primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // 📌 4) 사진 영역 밑에 Divider
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 8.dp)
                                .fillMaxWidth(0.8f),
                            thickness = 1.dp,
                            color = Variables.Color4
                        )

                        // 📌 5) 이제 TextField 등장
                        TextField(
                            value = diary,
                            onValueChange = { viewModel.updateDiary(it) },
                            placeholder = {
                                Text(
                                    text = "오늘의 일기를 작성해주세요",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif
                                )
                            },
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                fontFamily = FontFamily.SansSerif
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .animateContentSize(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Surface,
                                unfocusedContainerColor = Surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Primary,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedPlaceholderColor = Background,
                                unfocusedPlaceholderColor = Primary
                            )
                        )
                    }
                }

            }
        }

        // 하단 버튼
        Button(
            onClick = {
                viewModel.resetDetectedSentiment()
                viewModel.persistDiaryAndAnalyze(bottomNavController)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("다음으로", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }

        // 🔥 로딩 오버레이는 isAnalyzing 상태로만 제어 (통합된 로직)
        if (isAnalyzing) {
            AnalyzeLoadingOverlay()
        }

        // 달력 팝업 구현 부분
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
                    colors = CardDefaults.cardColors(containerColor = Surface),
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
                                weekdayContentColor = Color.Black ,
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
fun ExpandableDiaryCard(content: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)

    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = content,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (expanded) "접기 ▲" else "더보기 ▼",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
//createdAt이 밀리초 단위로 저장되어있기 때문에 사람이 읽을 수 있는 형식의 날짜로 변환시켜주는 함수
fun formatDate(time: Long): String {
    val sdf = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
    return sdf.format(Date(time))
}