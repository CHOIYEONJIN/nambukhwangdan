package com.example.nambukhwangdan.screens.inbox

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Grey
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.ui.theme.Surface

// ⭐️ 1. DiaryEntry 데이터 모델 추가
data class DiaryEntry(
    val id: Int,
    val date: String, // "YYYY/MM/DD" (예: "2023/10/25")
    val dayOfWeek: String, // "요일" (예: "토")
    val title: String,
    val content: String,
    val emotion: String // "감", "기", "슬", "노" 등
)

@Composable
fun InboxScreen() {
    val pretendard = FontFamily.Default

    var showMonthPopup by remember { mutableStateOf(false) }
    var showSortPopup by remember { mutableStateOf(false) }
    val currentMonthNumber = 10
    val currentMonthEnglish = "October"
    val currentSortOption = "시간순"

    // ⭐️ 2. 샘플 데이터 추가
    val diaryEntries = listOf(
        DiaryEntry(1, "2023/10/25", "수", "오늘의 제목 1", "내용이 짧거나 길 수 있습니다. 길어지면 두 줄까지 보여주고, 더 길면 ... 처리됩니다. 내용을 길게 써서 어떻게 되는지 봅시다. 정말 길게 써볼게요.", "감"),
        DiaryEntry(2, "2023/10/24", "화", "어제 일기", "오늘 일이 잘 풀려서 기분이 좋았습니다. 프로젝트 마감이 코앞이라 걱정했는데, 동료들의 도움으로 무사히 마무리할 수 있었어요. 퇴근 후 맛있는 저녁을 먹고 일찍 잠들었습니다.", "기"),
        DiaryEntry(3, "2023/10/23", "월", "회의 지옥", "하루 종일 회의만 하다가 끝났네요. 너무 피곤하고 지치는 하루였습니다. 다음 주에는 좀 더 생산적인 일정을 짜야겠어요. 그래도 커피는 맛있었네요.", "슬"),
        DiaryEntry(4, "2023/10/22", "일", "주말 나들이", "날씨가 너무 좋아서 근처 공원으로 나들이를 다녀왔습니다. 가족들과 함께 즐거운 시간을 보냈어요. 힐링되는 주말이었습니다. 다음 주도 화이팅!", "기"),
        DiaryEntry(5, "2023/10/21", "토", "예상치 못한 문제 발생", "갑자기 시스템에 큰 문제가 생겨서 주말인데도 출근했습니다. 문제 해결에 꼬박 8시간이 걸렸네요. 좀 화가 났지만, 결국 해결해서 다행입니다.", "노"),
        DiaryEntry(6, "2023/10/20", "금", "미래의 나에게", "이번 달 목표는 반드시 달성하자! 다음 주에 있을 중요한 발표 준비를 철저히 해야겠습니다. 매일 조금씩이라도 꾸준히 하는 것이 중요하다고 생각합니다.", "감"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

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
                sortOption = currentSortOption,
                pretendard = pretendard,
                onClick = { showSortPopup = true }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.width(350.dp).fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ⭐️ 3. diaryEntries 목록을 사용하여 항목 표시
            items(diaryEntries) { entry ->
                DiaryItem(entry = entry, pretendard = pretendard)
            }
        }
    }

    if (showMonthPopup) {
    }
    if (showSortPopup) {
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

@Composable
fun DiaryItem(entry: DiaryEntry, pretendard: FontFamily) {
    var isExpanded by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    val collapsedHeight = 70.dp

    // ⭐️ 1. entry 객체에서 날짜 정보 파싱 및 조합 (수정 없음)
    val day = entry.date.split('/').getOrElse(2) { "" } // 일(Day)은 세 번째 요소 (인덱스 2)
    val dayOfWeek = entry.dayOfWeek
    val displayDate = "$day $dayOfWeek" // 예: "25 수"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (isExpanded) 0.dp else collapsedHeight)
            .animateContentSize(animationSpec = tween(300))
            .clickable { isExpanded = !isExpanded },

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
                        // ⭐️ 4. 감정 표시 텍스트 적용
                        Text(entry.emotion, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(5.dp))

                    Box(
                        modifier = Modifier
                            .size(width = 30.dp, height = 14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        // ⭐️ 2. 수정된 날짜 표시 텍스트 적용 (수정 없음)
                        Text(
                            text = displayDate,
                            fontSize = 8.sp,
                            fontFamily = pretendard,
                            color = Color.Black
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(Grey)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(start = 10.dp, end = 10.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = entry.title,
                        fontFamily = pretendard,
                        fontSize = 16.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.content,
                        fontFamily = pretendard,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = if (isExpanded) androidx.compose.ui.text.style.TextOverflow.Clip else androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(20.dp))

                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Primary else Grey,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { isFavorite = !isFavorite }
                )

                Spacer(modifier = Modifier.width(20.dp))
            }

            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Surface.copy(alpha = 0.5f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⭐️ 일기 상세 내용 추가 UI 영역 (ID: ${entry.id})", fontFamily = pretendard, fontSize = 14.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DiaryScreenPreview() {
    InboxScreen()
}