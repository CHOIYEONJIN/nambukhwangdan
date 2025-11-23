package com.example.nambukhwangdan.screens.letters

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.nambukhwangdan.model.Diary
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.ui.theme.Variables
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLetterScreen(
    viewModel: DiaryViewModel,
    bottomNavController: NavController
) {
    LaunchedEffect(Unit) {
        viewModel.updateDiary("")   // 항상 입력 칸을 빈칸
    }
    var showReceiverDialog by remember { mutableStateOf(false) }
    val diary by viewModel.todayDiary.collectAsState()
    val dateMillis by viewModel.selectedDateMillis.collectAsState()
    val selectedEmotion by viewModel.selectedEmotion.collectAsState()
    val selectedSticker by viewModel.selectedSticker.collectAsState()
    val todayMillis = System.currentTimeMillis()
    val receiverName by viewModel.receiverName.collectAsState()

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
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
        }
        viewModel.setSelectedDate(calendar.timeInMillis)
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
                //편지 전송 날짜 및 발신인 선택
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .fillMaxWidth().background(Surface, RoundedCornerShape(10.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                    {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "${formatDate(dateMillis)} 23:00 의 ",
                                fontSize = 13.sp,
                                color = Variables.Color5,                 // 강조 색
                                fontWeight = FontWeight.SemiBold,         // 글자 강조
                                modifier = Modifier
                                    .clickable { showCalendar = true }
                                    .padding(2.dp)                        // 클릭 영역 확대
                            )
                            Text(
                                text = receiverName,
                                color = Variables.Color5,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { showReceiverDialog = true }
                            )

                            Text(text = "에게 보낼 편지", fontSize = 13.sp)
                        }
                        }
                        if (showReceiverDialog) {
                            Dialog(onDismissRequest = { showReceiverDialog = false }) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text("받을 사람을 선택해 주세요")

                                        Spacer(Modifier.height(12.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = (receiverName == "미래의 나"),
                                                onClick = {
                                                    viewModel.setReceiver("미래의 나")
                                                    showReceiverDialog = false
                                                }
                                            )
                                            Text("미래의 나")
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = (receiverName == "익명의 누군가"),
                                                onClick = {
                                                    viewModel.setReceiver("익명의 누군가")
                                                    showReceiverDialog = false
                                                }
                                            )
                                            Text("익명의 누군가")
                                        }
                                    }
                                }
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
            onClick = {
                val nickname = viewModel.nicknameToUse.value
                val newDiary = Diary(
                    id = UUID.randomUUID().toString(),
                    content = diary,
                    emotion = selectedEmotion ?: "",
                    sticker = selectedSticker,
                    date = dateMillis,
                    sendToFuture = receiverName == "미래의 나",
                    createdAt = System.currentTimeMillis(),
                    nickname = nickname             // 👈 ✔ 익명 / 실제 닉네임 반영됨
                )

                // 예: pastLetters에 추가 (샘플)
                viewModel.addDiary(newDiary)
                viewModel.saveDiary(newDiary)
                viewModel.clearForNewEntry()
                bottomNavController.navigate(Routes.Home) {
                    popUpTo(Routes.NewLetter) { inclusive = true }  // ← 이전 화면 제거
                    launchSingleTop = true                          // ← 중복 생성 방지
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Variables.Color5)
        ) {
            Text("저장하기", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }

        // 달력 팝업 구현 부분
        //TODO: Timepicker 구현해야함
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
