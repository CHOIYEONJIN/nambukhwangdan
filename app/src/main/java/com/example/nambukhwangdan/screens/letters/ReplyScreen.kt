package com.example.nambukhwangdan.screens.letters

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nambukhwangdan.screens.diary.AnalyzeLoadingOverlay
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.ui.theme.Variables
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReplyScreen(viewModel: DiaryViewModel,
                bottomNavController: NavController
) {
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
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        onResult = { uris ->
            // 선택된 URI 리스트를 ViewModel에 전달
            viewModel.setSelectedUris(uris)
        }
    )
    val replyToId by viewModel.replyToId.collectAsState()

// pastLetters 중 replyToId와 동일한 id를 가진 것만 필터링
    val replyLetter = pastLetters.find { it.id == replyToId }


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
                        //사진 추가 아이콘 박스
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Surface)
                                .border(1.dp, Variables.Color5, CircleShape)
                                .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, // 아직 사진 추가 페이지는 구현이 안돼서 누르면 앱 꺼져요
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "사진 추가",
                                tint = Variables.Color5,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(0.8f),
                            thickness = 1.dp,
                            color = Variables.Color4
                        )

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
                                cursorColor = Variables.Color4,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedPlaceholderColor = Variables.Color4,
                                unfocusedPlaceholderColor = Color.Black
                            )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isAnonymous by viewModel.isAnonymous.collectAsState()
                            Checkbox(
                                checked = isAnonymous,
                                onCheckedChange = { viewModel.onAnonymousCheckedChange(it) }
                            )
                            Text(text = "익명", fontSize = 12.sp)
                        }

                    }
                }

            }
        }

        // 하단 버튼
        Button(
            onClick = { viewModel.startAnalyze(bottomNavController) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Variables.Color5)
        ) {
            Text("다음으로", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        if (viewModel.isAnalyzing) {
            AnalyzeLoadingOverlay(bottomNavController)   // ← Overlay 컴포저블 호출
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
