package com.example.nambukhwangdan.screens.inbox

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Grey
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.viewmodel.LetterViewModel
import com.example.nambukhwangdan.model.Letter.Letter // ⭐️ 공식 Letter 모델 임포트
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val pretendard = FontFamily.Default

enum class LetterSortOption(val label: String) {
    TIME_DESC("시간순 (최신순)"),
    TIME_ASC("시간순 (오래된순)"),
    FAVORITE_FIRST("좋아요 먼저"),
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun InboxScreen(
    viewModel: LetterViewModel,
    navController: NavHostController
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedSortOption by remember { mutableStateOf(LetterSortOption.TIME_DESC) }

    // ⭐️ LetterViewModel의 allLetters Flow를 사용합니다.
    // 주의: 이 Flow는 현재 모든 편지(개인/공용)를 포함할 수 있으므로, 공용 편지함만 원한다면
    // ViewModel에 'inboundLetters' 같은 별도의 Flow를 구현해야 합니다.
    val letters: List<Letter> by viewModel.allLetters.collectAsState(initial = emptyList())


    val filteredLetters = remember(letters, selectedDate, selectedSortOption) {
        val monthlyFiltered = letters.filter { letter ->
            // createdAt 필드를 사용하여 편지가 작성된 시점을 기준으로 필터링
            val letterDate = Instant.ofEpochMilli(letter.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            letterDate.year == selectedDate.year && letterDate.monthValue == selectedDate.monthValue
        }

        when (selectedSortOption) {
            LetterSortOption.TIME_DESC -> monthlyFiltered.sortedByDescending { it.createdAt }
            LetterSortOption.TIME_ASC -> monthlyFiltered.sortedBy { it.createdAt }
            LetterSortOption.FAVORITE_FIRST -> monthlyFiltered.sortedWith(
                compareByDescending<Letter> { it.liked }
                    .thenByDescending { it.createdAt }
            )
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

        // UI 영역
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

        if (filteredLetters.isEmpty()) {
            EmptyInboxState()
        } else {
            LetterList(letters = filteredLetters, viewModel = viewModel)
        }
    }
    // 팝업 구현 영역
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
        LetterSortSelectionDialog(
            currentSortOption = selectedSortOption,
            onDismissRequest = { showSortPopup = false },
            onOptionSelected = { newOption ->
                selectedSortOption = newOption
                showSortPopup = false
            }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun LetterList(letters: List<Letter>, viewModel: LetterViewModel) {
    LazyColumn(
        modifier = Modifier
            .width(350.dp)
            .fillMaxHeight(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(letters, key = { it.id }) { letter ->
            var expanded by rememberSaveable(letter.id) { mutableStateOf(false) }
            LetterItem(
                letter = letter,
                pretendard = pretendard,
                isExpanded = expanded,
                onToggleFavorite = { id -> viewModel.toggleLike(id)},
                onToggleExpand = {expanded=!expanded},
                onDelete = { id -> viewModel.deleteLetter(id) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LetterItem(
    letter: Letter,
    pretendard: FontFamily,
    isExpanded: Boolean,
    onToggleFavorite:(String) -> Unit,
    onToggleExpand: () -> Unit,
    onDelete: (String) -> Unit
) {
    val isFavorite = letter.liked
    val collapsedHeight = 70.dp

    val displayDate = letter.createdAt.dateLabelForInbox() // createdAt 사용
    val favoriteStatus = if (isFavorite) "좋아요 됨" else "좋아요 안 됨"
    val titleText = letter.content.split("\n").firstOrNull().orEmpty()
    val indicatorColor = if (isFavorite) Primary else Grey.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (isExpanded) 0.dp else collapsedHeight)
            .animateContentSize(animationSpec = tween(300))
            .clickable { onToggleExpand() },
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
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = favoriteStatus,
                        tint = indicatorColor,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                    // 발송일자
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color.White)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayDate,
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
                        .height(collapsedHeight)
                        .padding(start = 10.dp, end = 10.dp, top = 10.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = titleText, // 편지 제목 또는 첫 줄
                        fontFamily = pretendard,
                        fontSize = 16.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (isFavorite) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "보낸 사람: ${letter.nickname}\n${letter.content}", // 발신자 닉네임과 내용 표시
                        fontFamily = pretendard,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis
                    )
                }

                // 우측 좋아요 토글 아이콘
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    tint = if (isFavorite) Primary else Grey,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onToggleFavorite(letter.id)}
                )

                Spacer(modifier = Modifier.width(20.dp))
            }

            // 확장된 상세 내용 영역
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
                            contentDescription = "delete letter",
                            modifier = Modifier.clickable { onDelete(letter.id) },
                            tint = Color(0xFFB00020)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⭐️ 삭제 버튼 클릭 시 이 편지는 영구 삭제됩니다.",
                            fontFamily = pretendard,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------
// 기존 MonthSelector, SortOptionPill, LetterSortSelectionDialog, dateLabelForInbox, EmptyInboxState는 유지
// -----------------------------------------------------------------------

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
            .size(width = 90.dp, height = 25.dp)
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

@Composable
fun LetterSortSelectionDialog(
    currentSortOption: LetterSortOption,
    onDismissRequest: () -> Unit,
    onOptionSelected: (LetterSortOption) -> Unit
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
                LetterSortOption.values().forEach { option ->
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

// --- 유틸리티 함수 (날짜 포매팅) ---
@RequiresApi(Build.VERSION_CODES.O)
private fun Long.dateLabelForInbox(): String {
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN)
    return formatter.format(date)
}

// --- Empty State 컴포넌트 ---
@Composable
private fun EmptyInboxState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "도착한 편지가 없어요", color = Color.DarkGray)
        Text(
            text = "다른 사용자에게 보낸 편지에 대한 답장을 기다려보세요.",
            color = Color.DarkGray,
            fontSize = 13.sp
        )
    }
}