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

// 폰트 임시 지정 (실제 폰트 경로에 맞게 수정 필요)
val pretendard = FontFamily.Default


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JournalScreen(
    viewModel: DiaryViewModel,
    navController: NavHostController
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedSortOption by remember { mutableStateOf(SortOption.TIME_DESC) }

    val diaries by viewModel.allRegularDiaries.collectAsState(initial = emptyList<Diary>())
    val allLetters by viewModel.allSentLetters.collectAsState(initial = emptyList<Diary>()) // 모든 편지 데이터

    val filteredDiaries = remember(diaries, selectedDate, selectedSortOption) {
        val monthlyFiltered = diaries.filter { diary ->
            val diaryDate = Instant.ofEpochMilli(diary.createdAt)
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

    // ⭐️ 3. UI에 표시할 월 정보는 selectedDate에서 가져옴
    val currentMonthNumber = selectedDate.monthValue
    val currentMonthEnglish = selectedDate.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
    val currentSortOptionLabel = selectedSortOption.label.split(" ").first() // "시간순", "좋아요순"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // UI 영역 (MonthSelector 클릭 시 showMonthPopup = true)
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

        if (filteredDiaries.isEmpty()) { // ⭐️ 필터링된 목록 사용
            EmptyJournalState()
        } else {
            // ⭐️ DiaryList에 모든 편지 목록을 전달
            DiaryList(
                diaries = filteredDiaries,
                allLetters = allLetters,
                viewModel = viewModel
            )
        }
    }

    if (showMonthPopup) {
        MonthOnlyDatePickerDialog(
            initialDate = selectedDate, // 현재 선택된 날짜를 초기값으로 전달
            onDismissRequest = { showMonthPopup = false },
            onDateSelected = { year, monthNumber ->
                // 선택된 년도와 월을 selectedDate에 반영하고 1일로 설정
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
                selectedSortOption = newOption // 선택된 옵션 업데이트 (자동 재정렬)
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
private fun DiaryList(diaries: List<Diary>, allLetters: List<Diary>, viewModel: DiaryViewModel) {
    // ⭐️ 맵을 사용하여 편지 ID로 내용을 빠르게 찾을 수 있도록 준비합니다.
    val letterMap = remember(allLetters) {
        allLetters.associateBy { it.id }
    }

    LazyColumn(
        modifier = Modifier
            .width(350.dp)
            .fillMaxHeight(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(diaries, key = { it.id }) { diary ->
            var expanded by rememberSaveable(diary.id) { mutableStateOf(false) }

            // ⭐️ 해당 일기(diary)가 참조한 원본 편지(letter)의 내용을 찾습니다.
            val originalLetterContent = remember(diary.replyToId, letterMap) {
                // replyToId가 null이 아니면, letterMap에서 해당 ID의 편지 내용을 찾고, 없으면 "원본 편지 없음"을 반환합니다.
                diary.replyToId?.let { letterMap[it]?.content } ?: "원본 편지 없음"
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

    val displayDayAndDayOfWeek = diary.createdAt.dayLabelForInbox() // 예: "25 수"
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

            // ⭐️ 접힌 상태 Row
            if (!isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(collapsedHeight) // ⭐️ 접힌 상태의 고정 높이 유지
                        .padding(end = 16.dp), // 좋아요 아이콘을 위한 오른쪽 패딩 추가
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

                    // 구분선
                    Spacer(modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(Grey))

                    // ⭐️ 본문만 2줄 표시 영역
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight() // 부모 Row 높이에 맞게 채움
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

                    // ⭐️ 좋아요 아이콘
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


            // ⭐️ 확장된 상세 내용 영역 (Expanded State)
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 1. 헤더 (날짜 및 수정/삭제 아이콘)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // ⭐️ 'n월 n일 의 나에게서 온 편지' 텍스트
                        Text(
                            text = "$displayMonthAndDay 의 나에게서 온 편지",
                            fontFamily = pretendard,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // ⭐️ 수정 및 삭제 아이콘
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

                    // 2. 원본 편지 내용 (어제 편지)
                    Text(
                        // ⭐️ 실제 원본 편지 내용을 표시합니다.
                        text = originalLetterContent,
                        fontFamily = pretendard,
                        fontSize = 15.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. 사진 Placeholder 3개 (이 부분은 필요에 따라 실제 로직으로 변경해야 합니다.)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Grey.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Photo $it", color = Grey)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 4. 일기 전문
                    Text(
                        text = diary.content,
                        fontFamily = pretendard,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 22.sp // 가독성 향상
                    )

                    // ⭐️ 확장된 내용의 하단 공간 확보
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
// --- 유틸리티 함수 (날짜 포매팅) ---
@RequiresApi(Build.VERSION_CODES.O)
private fun Long.dayLabelForInbox(): String {
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    val formatter = DateTimeFormatter.ofPattern("d E", Locale.KOREAN)
    return formatter.format(date)
}

// --- Empty State 컴포넌트 ---
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