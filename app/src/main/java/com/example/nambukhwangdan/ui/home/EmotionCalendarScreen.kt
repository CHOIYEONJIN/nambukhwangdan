package com.example.nambukhwangdan.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Surface
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EmotionCalendarScreen(viewModel: DiaryViewModel) {

    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedDate by remember { mutableStateOf(currentDate) }
    val diaries by viewModel.allDiaries.collectAsState()
    val selectedDiaries = diaries.filter { diary ->
        Instant.ofEpochMilli(diary.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate() == selectedDate
    }
    val calendarDates = getCalendarDates(currentDate.year, currentDate.monthValue)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Background)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Spacer(Modifier.height(30.dp))
        // ----------------- 상단 월 이동 -------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("◀", modifier = Modifier
                .clickable {
                    currentDate = currentDate.minusMonths(1)
                }
                .padding(10.dp),
                color=Color.Gray)
            Text(
                "${currentDate.monthValue} ${currentDate.month} ${currentDate.year}",
                fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color=Color.Gray
            )
            Text("▶", modifier = Modifier
                .clickable {
                    currentDate = currentDate.plusMonths(1)
                }
                .padding(10.dp),
                color=Color.Gray)
        }

        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier
            .padding(horizontal = 30.dp)
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(10.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp)){
        // ----------------- 요일 헤더 -------------------
            val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth().height(30.dp)) {
                items(weekDays) { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                ) }
            }

            Spacer(Modifier.height(8.dp))

        // ----------------- 날짜 렌더링 -------------------
            LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
                items(calendarDates.size) { index ->
                    val date = calendarDates[index]
                    val isThisMonth = date.monthValue == currentDate.monthValue
                    Column(horizontalAlignment = Alignment.CenterHorizontally){
                    //감정 표시 원
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .padding(4.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isThisMonth -> Color.White  // 💡 이번 달
                                        else -> Surface         // 🔹 이전/다음 달
                                    }
                                )
                                .clickable {
                                    if (!isThisMonth) {   // ◀ 이전 / ▶ 다음 달 이동
                                        currentDate = date.withDayOfMonth(1)
                                    }
                                    selectedDate = date
                                },
                            contentAlignment = Alignment.Center
                        ){}
                        Text(
                            text = date.dayOfMonth.toString(),
                            color = if (isThisMonth) Color.Black else Color.Gray,
                            fontSize = 10.sp
                        )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(selectedDiaries) { diary ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(color = Surface, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = diary.content,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCalendarDates(year: Int, month: Int): List<LocalDate> {
    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val daysInMonth = firstDayOfMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1=월요일 ~ 7=일요일

    // 🔹 이전 달
    val prevMonth = firstDayOfMonth.minusMonths(1)
    val prevMonthLastDay = prevMonth.lengthOfMonth()
    val leadingDays = firstDayOfWeek - 1  // 앞에 채울 개수

    val prevDays = (prevMonthLastDay - leadingDays + 1..prevMonthLastDay).map {
        prevMonth.withDayOfMonth(it)
    }

    // 🔹 현재 달
    val currentMonthDays = (1..daysInMonth).map {
        firstDayOfMonth.withDayOfMonth(it)
    }

    // 🔹 다음 달 → 달력의 전체 셀 수를 42개(6주 × 7칸) 기준으로 함
    val totalCells = 35
    val nextDaysCount = totalCells - (prevDays.size + currentMonthDays.size)

    val nextMonth = firstDayOfMonth.plusMonths(1)
    val nextDays = (1..nextDaysCount).map {
        nextMonth.withDayOfMonth(it)
    }

    return prevDays + currentMonthDays + nextDays
}