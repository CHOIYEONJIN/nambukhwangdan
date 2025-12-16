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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
import com.google.common.io.Files.append
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val pretendard = FontFamily.Default

enum class LetterSortOption(val label: String) {
    TIME_DESC("최신순"),
    TIME_ASC("오래된순"),
    FAVORITE_FIRST("좋아요순"),
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
            .fillMaxWidth()
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

    // DiaryItem과 유사한 날짜 형식 사용
    val displayDateFormatted = letter.createdAt.dateLabelForInbox() // 예: "월요일"
    val displayDateVerbose = Instant.ofEpochMilli(letter.createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)) // 예: "12월 17일"

    val indicatorColor = if (isFavorite) Primary else Grey.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .animateContentSize(animationSpec = tween(300))
            .clickable { onToggleExpand() },

        shape = RoundedCornerShape(10.dp), // DiaryItem과 동일
        colors = CardDefaults.cardColors(containerColor = Surface), // DiaryItem과 동일
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // DiaryItem과 동일
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {

            // =========================================================
            // A. 축소 상태 (isExpanded = false)
            // =========================================================
            if (!isExpanded) {
                Row(
                    // 좌측 70dp 영역 제거 -> 내용 영역이 전체 폭을 차지하도록 패딩 조정
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(collapsedHeight)
                        .padding(start = 16.dp, end = 16.dp), // 좌우 패딩만 적용
                    verticalAlignment = Alignment.CenterVertically
                ) {


                    Column(
                        modifier = Modifier
                            .weight(1f) // 남은 공간 모두 차지
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center // 중앙 정렬
                    ) {
                        // 1. 편지 요약 제목: (날짜)에 (보낸사람) 보낸 편지 (AnnotatedString 사용)
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                                ) {
                                    append(displayDateFormatted)
                                }
                                append("에 ")

                                // (보낸사람) 부분
                                withStyle(style = SpanStyle(
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                                ) {
                                    append(letter.nickname)
                                }
                                // 나머지 부분
                                append("님이 보낸 편지")
                            },
                            fontFamily = pretendard,
                            fontSize = 16.sp,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // 2. 내용 미리보기
                        Text(
                            text = letter.content,
                            fontFamily = pretendard,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 1, // 1줄로 제한
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // ⭐️ 우측 좋아요 토글 아이콘 (DiaryItem과 동일 위치)
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (isFavorite) Primary else Grey,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onToggleFavorite(letter.id)}
                    )
                }
            }


            // =========================================================
            // B. 확장 상태 (isExpanded = true) - DiaryItem 디자인 복제
            // =========================================================
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp) // DiaryItem과 동일한 패딩
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 1. 제목: 받은 편지 제목 ('M월 d일' 형식 사용)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${displayDateVerbose}에 ${letter.nickname}님에게서 온 편지",
                                fontFamily = pretendard,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold // DiaryItem과 동일
                            )
                            // ⭐️ Sticker 영역 제거 (LetterItem에는 스티커 없음)
                        }

                        // 2. 액션 버튼: 삭제 버튼만 표시 (DiaryItem의 Edit 자리에 삭제 버튼 배치)
                        Row {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Letter",
                                // ⭐️ DiaryItem과 동일한 색상/사이즈
                                tint = Grey,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onDelete(letter.id) } // ⭐️ 기능 유지
                            )
                        }
                    }

                    // 3. 첫 번째 Divider (DiaryItem과 동일한 간격/스타일)
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp),
                        color = Color.LightGray,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. 본문 내용 (LetterItem에는 원본 편지 내용이 없으므로, 편지 내용이 바로 옵니다)
                    Text(
                        // ⭐️ 편지 전체 내용을 표시
                        text = letter.content,
                        fontFamily = pretendard,
                        fontSize = 14.sp,
                        color = Color.DarkGray, // DiaryItem과 동일
                        lineHeight = 22.sp // DiaryItem과 동일
                    )

                    // 5. 하단 Spacer (DiaryItem과 동일)
                    Spacer(modifier = Modifier.height(16.dp))
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
            color = Grey
        )
        Spacer(modifier = Modifier.width(2.dp))

        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Change Sort Order",
            modifier = Modifier.size(16.dp),
            tint = Grey
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