package com.example.nambukhwangdan.screens.journal

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nambukhwangdan.model.Diary
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun JournalScreen(
    viewModel: DiaryViewModel,
    onWriteDiary: () -> Unit
) {
    val diaries by viewModel.allDiaries.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        HeaderSection(onWriteDiary = onWriteDiary)
        Spacer(modifier = Modifier.height(12.dp))
        DateHeader(referenceDateMillis = diaries.firstOrNull()?.date)
        Spacer(modifier = Modifier.height(12.dp))
        TimelineList(entries = diaries)
    }
}

@Composable
private fun HeaderSection(onWriteDiary: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB1D0B1)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "나", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "일기장",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Text(
                    text = "오늘 하루를 기록해보세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
        Button(
            onClick = onWriteDiary,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("일기 쓰기", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DateHeader(referenceDateMillis: Long?) {
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.KOREAN)
    val detailFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd EEE", Locale.KOREAN)
    val date = Instant.ofEpochMilli(referenceDateMillis ?: System.currentTimeMillis())
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = monthFormatter.format(date), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text(text = detailFormatter.format(date), color = Color.DarkGray)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* TODO: 날짜 선택 기능 */ }) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "달력",
                    tint = Color.Black
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .clickable { /* TODO: 정렬 옵션 */ }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(text = "시간순", fontWeight = FontWeight.Medium, color = Color.Black)
            }
        }
    }
}

@Composable
private fun TimelineList(entries: List<Diary>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        itemsIndexed(entries.sortedByDescending(Diary::date)) { index, entry ->
            Row(modifier = Modifier.fillMaxWidth()) {
                TimelineIndicator(
                    isFirst = index == 0,
                    isLast = index == entries.lastIndex
                )
                Spacer(modifier = Modifier.width(12.dp))
                JournalCard(entry = entry)
            }
        }
    }
}

@Composable
private fun TimelineIndicator(isFirst: Boolean, isLast: Boolean) {
    Column(
        modifier = Modifier.width(38.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isFirst) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(16.dp)
                    .background(Color(0xFFA2C0A2))
            )
        }
        Canvas(modifier = Modifier.size(32.dp)) {
            drawCircle(color = Color(0xFF8FC48F))
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(48.dp)
                    .background(Color(0xFFA2C0A2))
            )
        }
    }
}

@Composable
private fun JournalCard(entry: Diary) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.dayLabel(),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B6B6B)
                )
                Icon(
                    imageVector = if (entry.liked) Icons.Rounded.Favorite else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = if (entry.liked) Primary else Color(0xFF6B6B6B)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.content,
                color = Color.Black,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.detailLabel(),
                color = Color(0xFF8D8D8D),
                fontSize = 12.sp
            )
        }
    }
}

private fun Diary.dayLabel(): String {
    val date = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
    val dayOfWeek = when (date.dayOfWeek.value) {
        1 -> "월"
        2 -> "화"
        3 -> "수"
        4 -> "목"
        5 -> "금"
        6 -> "토"
        else -> "일"
    }
    return "${date.dayOfMonth} $dayOfWeek"
}

private fun Diary.detailLabel(): String {
    return DateTimeFormatter.ofPattern("yyyy.MM.dd EEE", Locale.KOREAN)
        .format(Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()))
}
