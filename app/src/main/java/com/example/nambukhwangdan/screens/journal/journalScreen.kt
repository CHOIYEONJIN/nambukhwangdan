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
import androidx.compose.foundation.layout.heightIn
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
import com.example.nambukhwangdan.components.MonthOnlyDatePickerDialog
import com.example.nambukhwangdan.model.Diary
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
    viewModel: DiaryViewModel
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedSortOption by remember { mutableStateOf(SortOption.TIME_DESC) }

    val diaries by viewModel.allDiaries.collectAsState(initial = emptyList<Diary>())


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
                onClick = { showMonthPopup = true } // ⭐️ 팝업 열기
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
            DiaryList(diaries = filteredDiaries, viewModel = viewModel) // ⭐️ 필터링된 목록 전달
        }
    }

    // ⭐️ 4. 팝업 구현 영역: 여기에 MonthOnlyDatePickerDialog 연결
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
private fun DiaryList(diaries: List<Diary>, viewModel: DiaryViewModel) {
    LazyColumn(
        modifier = Modifier
            .width(350.dp)
            .fillMaxHeight(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(diaries, key = { it.id }) { diary ->
            // ⭐️ 기능 유지: 확장 상태 관리
            var expanded by rememberSaveable(diary.id) { mutableStateOf(false) }
            DiaryItem(
                diary = diary,
                pretendard = pretendard,
                isExpanded = expanded,
                onLiked ={ id -> viewModel.toggleLike(id)},
                onToggle = {expanded=!expanded},
                onDelete = { id -> viewModel.deleteDiary(id) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiaryItem(diary: Diary, pretendard: FontFamily, isExpanded: Boolean, onLiked:(String) -> Unit, onToggle: () -> Unit, onDelete: (String) -> Unit) {
    val isLiked = diary.liked
    val collapsedHeight = 70.dp

    val displayDayAndDayOfWeek = diary.createdAt.dayLabelForInbox() // 예: "25 수"
    val emotionText = diary.sticker.orEmpty().ifBlank { "💭" }
    val titleText = diary.content.split("\n").firstOrNull().orEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (isExpanded) 0.dp else collapsedHeight)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = collapsedHeight),
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(collapsedHeight) // collapsedHeight 만큼 높이 설정
                        .padding(start = 10.dp, end = 10.dp, top = 10.dp), // 상단 패딩 추가
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = titleText,
                        fontFamily = pretendard,
                        fontSize = 16.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = diary.content,
                        fontFamily = pretendard,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        // ⭐️ 기능 유지: 확장 상태에 따라 maxLines 변경
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis
                    )
                }

                // 3. 좋아요 아이콘
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isLiked) Primary else Grey,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onLiked(diary.id)}
                )

                Spacer(modifier = Modifier.width(20.dp))
            }

            // 4. 확장된 상세 내용 영역
            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Surface.copy(alpha = 0.5f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(){
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "delete diary",
                            modifier = Modifier.clickable { onDelete(diary.id) },
                            tint = Color(0xFFB00020) // 오류/삭제 계열 색상
                        )
                        Text(
                            "⭐️ 일기 상세 내용 추가 UI 영역 (ID: ${diary.id})",
                            fontFamily = pretendard,
                            fontSize = 14.sp
                        )
                    }

                }
            }
        }
    }
}

// --- 유틸리티 함수 (날짜 포매팅) ---

/**
 * Diary의 createdAt (Long)에서 "일 요일" 형식의 문자열을 반환합니다. (예: "25 수")
 */
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

// --- Preview (ViewModel 의존성으로 인해 주석 처리) ---

// @Preview(showBackground = true, showSystemUi = true)
// @Composable
// private fun JournalScreenPreview() {
//      // Preview를 실행하려면 DiaryViewModel의 더미 구현이 필요합니다.
//      // JournalScreen(viewModel = DummyDiaryViewModel())
// }
