package com.example.nambukhwangdan.screens.journal

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.nambukhwangdan.components.MonthOnlyDatePickerDialog
import com.example.nambukhwangdan.model.Diary.Diary
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetter
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Grey
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.nambukhwangdan.model.TomorrowLetter.TomorrowLetterEntity

// 폰트 임시 지정 (실제 폰트 경로에 맞게 수정 필요)
val pretendard = FontFamily.Default


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JournalScreen(
    viewModel: DiaryViewModel, // ⭐️ DiaryViewModel만 사용
    navController: NavHostController
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedSortOption by remember { mutableStateOf(SortOption.TIME_DESC) }

    // ⭐️ 1. 모든 Diary 가져오기 (가시성 문제 해결)
    val diaries by viewModel.allDiaries.collectAsState(initial = emptyList<Diary>())

    val allTomorrowLetters by viewModel.allTomorrowLetters.collectAsState(initial = emptyList<TomorrowLetter>())

    val letterMap = remember(allTomorrowLetters) {
        val letters = allTomorrowLetters as List<TomorrowLetter>
        letters.associateBy { it.id }
    }

    // ⭐️ 4. 모든 diaries를 기반으로 필터링 및 정렬합니다.
    val filteredDiaries = remember(diaries, selectedDate, selectedSortOption) {
        val monthlyFiltered = diaries.filter { diary ->
            // diary.date 필드를 사용하는 것이 정확합니다.
            val diaryDate = Instant.ofEpochMilli(diary.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            diaryDate.year == selectedDate.year && diaryDate.monthValue == selectedDate.monthValue
        }

        when (selectedSortOption) {
            SortOption.TIME_DESC -> monthlyFiltered.sortedByDescending { it.createdAt }
            SortOption.TIME_ASC -> monthlyFiltered.sortedBy { it.createdAt }
            SortOption.LIKED_DESC -> monthlyFiltered.sortedByDescending { it.liked }
        }
    }

    var showMonthPopup by remember { mutableStateOf(false) }
    var showSortPopup by remember { mutableStateOf(false) }

    val currentMonthNumber = selectedDate.monthValue
    val currentMonthEnglish = selectedDate.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
    val currentSortOptionLabel = selectedSortOption.label.split(" ").first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Row(
            modifier = Modifier
                .width(350.dp)
                .height(40.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonthSelector(
                monthNumber = currentMonthNumber,
                monthEnglish = currentMonthEnglish,
                pretendard = pretendard,
                onClick = { showMonthPopup = true }
            )
            Spacer(modifier = Modifier.weight(1f))
            SortOptionPill(
                sortOption = currentSortOptionLabel,
                pretendard = pretendard,
                onClick = { showSortPopup = true }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredDiaries.isEmpty()) {
            EmptyJournalState()
        } else {
            // ⭐️ DiaryList에 생성한 TomorrowLetter Map을 전달합니다.
            DiaryList(
                diaries = filteredDiaries,
                letterMap = letterMap,
                viewModel = viewModel
            )
        }
    }

    if (showMonthPopup) {
        MonthOnlyDatePickerDialog(
            initialDate = selectedDate,
            onDismissRequest = { showMonthPopup = false },
            onDateSelected = { year, monthNumber ->
                selectedDate = LocalDate.of(year, monthNumber, 1)
                showMonthPopup = false
            }
        )
    }

    if (showSortPopup) {
        SortSelectionDialog(
            currentSortOption = selectedSortOption,
            onDismissRequest = { showSortPopup = false },
            onOptionSelected = { newOption ->
                selectedSortOption = newOption
                showSortPopup = false
            }
        )
    }
}


@Composable
fun MonthSelector(
    monthNumber: Int,
    monthEnglish: String,
    pretendard: FontFamily,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .wrapContentSize(),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = monthNumber.toString(),
            fontFamily = pretendard,
            fontSize = 30.sp,
            color = Primary,
            modifier = Modifier.alignByBaseline()
        )
        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = monthEnglish,
            fontFamily = pretendard,
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier.alignByBaseline()
        )

        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Change Month",
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically)
                .offset(y = (-2).dp),
            tint = Color.Black
        )
    }
}


@Composable
fun SortOptionPill(
    sortOption: String,
    pretendard: FontFamily,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .size(width = 70.dp, height = 25.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = sortOption,
            fontFamily = pretendard,
            fontSize = 12.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(2.dp))

        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Change Sort Order",
            modifier = Modifier.size(16.dp),
            tint = Color.Black
        )
    }
}

enum class SortOption(val label: String) {
    TIME_DESC("시간순 (최신순)"),
    TIME_ASC("시간순 (오래된순)"),
    LIKED_DESC("좋아요순")
}

@Composable
fun SortSelectionDialog(
    currentSortOption: SortOption,
    onDismissRequest: () -> Unit,
    onOptionSelected: (SortOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("정렬 기준 선택", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                SortOption.values().forEach { option ->
                    val isSelected = option == currentSortOption
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onOptionSelected(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = option.label,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("닫기")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DiaryList(
    diaries: List<Diary>,
    letterMap: Map<String, TomorrowLetter>,
    viewModel: DiaryViewModel
){
    LazyColumn(
        modifier = Modifier
            .width(350.dp)
            .fillMaxHeight(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(diaries, key = { it.id }) { diary ->
            var expanded by rememberSaveable(diary.id) { mutableStateOf(false) }

            // ⭐️ 해당 일기(diary)가 참조한 원본 TomorrowLetter의 내용을 찾습니다.
            val originalLetterContent = remember(diary.replyToId, letterMap) {
                // diary.replyToId가 TomorrowLetter의 ID를 담고 있습니다.
                diary.replyToId?.let { letterId ->
                    letterMap[letterId]?.content ?: "원본 편지 (ID: $letterId) 내용을 찾을 수 없습니다."
                } ?: "원본 편지 없음 (일반 일기)"
            }

            DiaryItem(
                diary = diary,
                pretendard = pretendard,
                isExpanded = expanded,
                // ⭐️ 실제 원본 편지 내용 전달
                originalLetterContent = originalLetterContent,
                onLiked ={ id -> viewModel.toggleLike(id)},
                onToggle = {expanded=!expanded},
                onEdit = { /* TODO: 수정 화면으로 이동 로직 */ },
                onDelete = { id -> viewModel.deleteDiary(id) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiaryItem(
    diary: Diary,
    pretendard: FontFamily,
    isExpanded: Boolean,
    originalLetterContent: String,
    onLiked:(String) -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (String) -> Unit
) {
    val isLiked = diary.liked
    val collapsedHeight = 70.dp

    val displayMonthAndDay = Instant.ofEpochMilli(diary.createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN))

    val displayDayAndDayOfWeek = diary.createdAt.dayLabelForInbox()
    val emotionText = diary.sticker.orEmpty().ifBlank { "💭" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
            .clickable { onToggle() },

        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {

            if (!isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(collapsedHeight)
                        .padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier
                            .width(70.dp)
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emotionText, fontSize = 16.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(5.dp))

                        Box(
                            modifier = Modifier
                                .size(width = 30.dp, height = 14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayDayAndDayOfWeek,
                                fontSize = 8.sp,
                                fontFamily = pretendard,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(Grey))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = 10.dp, end = 10.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = diary.content,
                            fontFamily = pretendard,
                            fontSize = 14.sp,
                            color = Color.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isLiked) Primary else Grey,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onLiked(diary.id)}
                    )
                }
            }


            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$displayMonthAndDay 의 일기",
                                fontFamily = pretendard,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            diary.sentimentLabel?.let { label ->
                                Spacer(modifier = Modifier.width(8.dp))
                                SentimentBadge(label)
                            }
                        }

                        Row {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Diary",
                                tint = Grey,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onEdit() }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Diary",
                                tint = Color(0xFFB00020),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onDelete(diary.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ⭐️ 원본 편지 내용 표시
                    Text(
                        text = "원본 편지: ${originalLetterContent}",
                        fontFamily = pretendard,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Grey.copy(alpha = 0.2f))
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Photo $it", color = Grey)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = diary.content,
                        fontFamily = pretendard,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


@Composable
private fun SentimentBadge(label: String) {
    val color = when (label.lowercase()) {
        "positive", "긍정" -> Primary
        "negative", "부정" -> Color(0xFFF44336)
        else -> Color(0xFFFFC107)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = color, fontSize = 10.sp)
    }
}
@RequiresApi(Build.VERSION_CODES.O)
private fun Long.dayLabelForInbox(): String {
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    val formatter = DateTimeFormatter.ofPattern("d E", Locale.KOREAN)
    return formatter.format(date)
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