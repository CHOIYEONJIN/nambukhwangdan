package com.example.nambukhwangdan.screens.letters

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.ui.theme.Variables
import com.example.nambukhwangdan.viewmodel.LetterViewModel
import com.example.nambukhwangdan.viewmodel.LetterViewModel.ReceiverOption
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLetterScreen(
    viewModel: LetterViewModel = hiltViewModel(),
    bottomNavController: NavController
) {
    val letterText by viewModel.letterContent.collectAsState()
    val receiverName by viewModel.receiverName.collectAsState()
    val dateMillis by viewModel.selectedDateMillis.collectAsState()
    val receiverOption by viewModel.receiverOption.collectAsState()
    val scheduledAt by viewModel.scheduledAt.collectAsState()
    val selectedHour by viewModel.selectedHour.collectAsState()
    val selectedMinute by viewModel.selectedMinute.collectAsState()

    var showReceiverDialog by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
    val timePickerState = rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute, is24Hour = true)

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { viewModel.setSelectedDate(it) }
    }

    LaunchedEffect(selectedHour, selectedMinute) {
        timePickerState.hour = selectedHour
        timePickerState.minute = selectedMinute
    }

    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
        }
        viewModel.setSelectedDate(calendar.timeInMillis)
        viewModel.setSelectedTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    val dateFormatter = remember { SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.KOREA) }
    val scheduleText = remember(scheduledAt) { dateFormatter.format(Date(scheduledAt)) + " " + timeFormatter.format(Date(scheduledAt)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Variables.Color4)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(scheduleText)
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "${scheduleText} 에 ",
                                fontSize = 13.sp,
                                color = Primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { showCalendar = true }
                                    .padding(2.dp)
                            )
                            Text(
                                text = "시간 변경",
                                fontSize = 12.sp,
                                color = Primary,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { showTimePicker = true }
                            )
                            Text(
                                text = receiverName,
                                color = Primary,
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
                                            selected = (receiverOption == ReceiverOption.FUTURE_SELF),
                                            onClick = {
                                                viewModel.setReceiver(ReceiverOption.FUTURE_SELF)
                                                showReceiverDialog = false
                                            }
                                        )
                                        Text("미래의 나")
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = (receiverOption == ReceiverOption.RANDOM_ANONYMOUS),
                                            onClick = {
                                                viewModel.setReceiver(ReceiverOption.RANDOM_ANONYMOUS)
                                                showReceiverDialog = false
                                            }
                                        )
                                        Text("익명의 누군가(Anonymous Someone)")
                                    }
                                }
                            }
                        }
                    }
                }

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
                        TextField(
                            value = letterText,
                            onValueChange = { viewModel.updateContent(it) },
                            placeholder = {
                                Text(
                                    text = "편지의 내용을 입력해주세요",
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
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.sendLetter {
                    bottomNavController.navigate(Routes.Home) {
                        popUpTo(Routes.NewLetter) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("저장하기", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }

        if (showCalendar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
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
                        val headlineStr = remember(datePickerState.selectedDateMillis) {
                            val millis = datePickerState.selectedDateMillis ?: dateMillis
                            dateFormatter.format(Date(millis))
                        }
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
                            headline = {
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
                        Button(
                            onClick = {
                                showCalendar = false
                                viewModel.setSelectedDate(datePickerState.selectedDateMillis ?: dateMillis)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("확인", color = Color.White)
                        }
                    }
                }
            }
        }

        if (showTimePicker) {
            Dialog(onDismissRequest = { showTimePicker = false }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TimePicker(state = timePickerState)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.setSelectedTime(timePickerState.hour, timePickerState.minute)
                                showTimePicker = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) { Text("확인", color = Color.White) }
                    }
                }
            }
        }
    }
}

fun formatDate(time: Long): String {
    val sdf = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
    return sdf.format(Date(time))
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
