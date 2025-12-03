package com.example.nambukhwangdan.ui.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.nambukhwangdan.model.Letter.Letter
import com.example.nambukhwangdan.model.Diary.Diary
import com.example.nambukhwangdan.screens.diary.formatDate
import com.example.nambukhwangdan.screens.journal.DiaryItem
import com.example.nambukhwangdan.screens.journal.pretendard
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import com.example.nambukhwangdan.viewmodel.LetterViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EmotionCalendarScreen(
    diaryViewModel: DiaryViewModel,    // ✅ 이 화면은 "일기" 전용 ViewModel만 사용
    letterViewModel: LetterViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        letterViewModel.syncLettersFromFirestore()
        diaryViewModel.syncDiaries()
    }
    // 🔹 오늘 날짜 기준
    var currentDate by remember { mutableStateOf(LocalDate.now()) }   // 현재 달
    var selectedDate by remember { mutableStateOf(currentDate) }     // 목록에 사용할 선택 날짜

    LaunchedEffect(currentDate) {
        diaryViewModel.setMonth(currentDate.year, currentDate.monthValue)
    }

    val monthDiaries by diaryViewModel.currentMonthDiaries.collectAsState()
    val daySentiments = remember(monthDiaries) {
        monthDiaries.groupBy { diary ->
            Instant.ofEpochMilli(diary.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.mapValues { entry -> resolveDaySentiment(entry.value) }
    }

    // 🔹 선택한 날짜에 해당하는 일기들만 필터링
    val selectedDiaries = monthDiaries.filter { diary ->
        Instant.ofEpochMilli(diary.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate() == selectedDate
    }
    val letters by letterViewModel.allLetters.collectAsState()
    Log.d("LettersTest", "allLetters = $letters")

    val selectedLetters = letters.filter { letter ->
        Instant.ofEpochMilli(letter.createdAt)   // or letter.date (둘 중 사용 중인 필드)
            .atZone(ZoneId.systemDefault())
            .toLocalDate() == selectedDate
    }
    // 🔹 현재 월 달력에 들어갈 날짜들 (이전/다음 달 포함)
    val calendarDates = getCalendarDates(currentDate.year, currentDate.monthValue)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
        ) {
            // ------------ (1) 달력 영역 ------------
            item {
                Spacer(Modifier.height(30.dp))

                // 🔸 상단 월 이동 바
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 이전 달
                    Text(
                        "◀",
                        modifier = Modifier
                            .clickable { currentDate = currentDate.minusMonths(1) }
                            .padding(10.dp),
                        color = Color.Gray
                    )

                    // 가운데: "2025년 11월" 이런 느낌으로 표시하고 싶으면 나중에 포맷 바꿔도 됨
                    Text(
                        text = "${currentDate.year}년 ${currentDate.monthValue}월",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )

                    // 다음 달
                    Text(
                        "▶",
                        modifier = Modifier
                            .clickable { currentDate = currentDate.plusMonths(1) }
                            .padding(10.dp),
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 🔸 달력 카드
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(10.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    // ---- 요일 헤더 ----
                    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    ) {
                        items(weekDays) { day ->
                            Text(
                                text = day,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // ---- 날짜 셀 렌더링 ----
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(330.dp)
                    ) {
                        items(calendarDates.size) { index ->
                            val date = calendarDates[index]
                            val isThisMonth = date.monthValue == currentDate.monthValue
                            val isSelected = date == selectedDate

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // 동그란 날짜 셀
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .padding(4.dp)
                                        .shadow(2.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> Color(0xFFE3F2FD) // 선택된 날짜 배경
                                                isThisMonth -> Color.White      // 현재 달
                                                else -> Surface                 // 이전/다음 달
                                            }
                                        )
                                        .clickable {
                                            // 다른 달 날짜를 누르면 해당 달로 이동
                                            if (!isThisMonth) {
                                                currentDate = date.withDayOfMonth(1)
                                            }
                                            selectedDate = date
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    daySentiments[date]?.let { sentiment ->
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(8.dp)
                                                .background(sentimentColor(sentiment), CircleShape)
                                        )
                                    }
                                }
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    color = if (isThisMonth) Color.Black else Color.Gray,
                                    fontSize = 12.sp)
                            }

                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // ------------ (2) 선택 날짜의 일기 리스트 ------------
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedDiaries.isEmpty()) {
                        // 이 날짜에 일기가 없는 경우
                        Text(
                            text = "이 날에는 아직 기록이 없어요.",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    } else {
                        // 이 날짜에 있는 일기 카드들
                        selectedDiaries.forEach { diary ->
                            var expanded by rememberSaveable(diary.id) { mutableStateOf(false) }

                            DiaryItem(
                                diary = diary,
                                pretendard = pretendard,
                                isExpanded = expanded,
                                onLiked = { id -> diaryViewModel.toggleLike(id) },
                                onToggle = { expanded = !expanded },
                                onDelete = { id -> diaryViewModel.deleteDiary(id) }
                            )
                        }
                        selectedLetters.forEach { letter ->
                            var expanded by rememberSaveable(letter.id) { mutableStateOf(false) }

                            LetterItem(
                                letter = letter,
                                isExpanded = expanded,
                                onToggle = { expanded = !expanded },
                                onLiked = { id -> letterViewModel.toggleLike(id) },
                                onDelete = { id -> letterViewModel.deleteLetter(id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun LetterItem(
    letter: Letter,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onLiked: (String) -> Unit,
    onDelete: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .background(Color(0xFFFFEDED), RoundedCornerShape(10.dp)) // 💗 Letter 색
            .clickable { onToggle() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = "${formatDate(letter.createdAt)} • ${letter.nickname}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = letter.content,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { onLiked(letter.id) }) {
                Text(if (letter.liked) "★ 북마크 해제" else "☆ 북마크")
            }
            TextButton(onClick = { onDelete(letter.id) }) {
                Text("삭제")
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
    val leadingDays = firstDayOfWeek - 1  // 앞에 채울 개수 (월~일 기준)

    val prevDays = (prevMonthLastDay - leadingDays + 1..prevMonthLastDay).map {
        prevMonth.withDayOfMonth(it)
    }

    // 🔹 현재 달
    val currentMonthDays = (1..daysInMonth).map {
        firstDayOfMonth.withDayOfMonth(it)
    }

    // 🔹 다음 달 → 35칸(5주) 기준
    val totalCells = 35
    val nextDaysCount = totalCells - (prevDays.size + currentMonthDays.size)

    val nextMonth = firstDayOfMonth.plusMonths(1)
    val nextDays = (1..nextDaysCount).map {
        nextMonth.withDayOfMonth(it)
    }

    return prevDays + currentMonthDays + nextDays
}


private fun resolveDaySentiment(diaries: List<Diary>): String? {
    val labels = diaries.mapNotNull { it.sentimentLabel }
    if (labels.isEmpty()) return null
    return labels.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
}

private fun sentimentColor(label: String): Color = when (label.lowercase()) {
    "positive", "긍정" -> Color(0xFF8BC34A)
    "negative", "부정" -> Color(0xFFF44336)
    else -> Color(0xFFFFC107)
}
