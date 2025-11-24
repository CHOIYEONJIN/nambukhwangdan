package com.example.nambukhwangdan.screens.journal

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    viewModel: DiaryViewModel
) {
    val diaries by viewModel.allDiaries.collectAsState()
    val sortedDiaries = diaries.sortedByDescending { it.createdAt }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        JournalHeader()
        Spacer(modifier = Modifier.height(12.dp))
        JournalDateToolbar()
        Spacer(modifier = Modifier.height(12.dp))

        if (sortedDiaries.isEmpty()) {
            EmptyJournalState()
        } else {
            DiaryList(diaries = sortedDiaries)
        }
    }
}

@Composable
private fun JournalHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "일기장",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )
        Text(
            text = "오늘 하루를 기록해보세요",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8B8B8B)
        )
    }
}

@Composable
private fun JournalDateToolbar() {
    val today = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
    val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatter.format(today),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "시간순",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                color = Color(0xFF6B6B6B),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DiaryList(diaries: List<Diary>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(diaries, key = { it.id }) { diary ->
            var expanded by rememberSaveable(diary.id) { mutableStateOf(false) }
            DiaryCard(
                diary = diary,
                expanded = expanded,
                onToggle = { expanded = !expanded }
            )
        }
    }
}

@Composable
private fun DiaryCard(
    diary: Diary,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onToggle() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DiarySticker(sticker = diary.sticker)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = diary.createdAt.dayLabel(),
                            color = Color(0xFF404040),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = diary.createdAt.fullDateLabel(),
                            color = Color(0xFF8D8D8D),
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = if (diary.liked) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (diary.liked) Primary else Color(0xFF8D8D8D)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = diary.content,
                color = Color.Black,
                fontSize = 14.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            if (!diary.sticker.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Primary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = diary.sticker.orEmpty(),
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DiarySticker(sticker: String?) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (sticker.isNullOrBlank()) "💭" else sticker,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun EmptyJournalState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "아직 작성된 일기가 없어요", color = Color.DarkGray)
        Text(
            text = "하단의 일기 탭에서 새로운 일기를 작성해 보세요",
            color = Color.DarkGray,
            fontSize = 13.sp
        )
    }
}

private fun Long.dayLabel(): String {
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    val monthFormatter = DateTimeFormatter.ofPattern("d일 E", Locale.KOREAN)
    return monthFormatter.format(date)
}

private fun Long.fullDateLabel(): String {
    val dateTime = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (EEE)", Locale.KOREAN)
    return formatter.format(dateTime)
}
