package com.example.nambukhwangdan.screens.diary

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.viewmodel.DiaryViewModel

@Composable
fun AnalyzeResultScreen_old(
    viewModel: DiaryViewModel,
    bottomNavController: NavController
) {


    val dairy by viewModel.todayDiary.collectAsState()
    val detected by viewModel.detectedEmotion.collectAsState()
    val selectedEmotion by viewModel.selectedEmotion.collectAsState()
    val selectedSticker by viewModel.selectedSticker.collectAsState()

    val emotionCats = listOf("긍정", "중립", "부정")
    val safeEmotion by remember { derivedStateOf { selectedEmotion ?: detected ?: "기쁨" } }
    val detailStickers = remember(safeEmotion) {
        when (safeEmotion) {
            "긍정" -> listOf("기쁨", "감사", "뿌듯", "설렘", "안도")
            "중립" -> listOf("차분", "평온", "무던", "담담", "관망")
            "부정" -> listOf("분노", "슬픔", "피곤", "불안", "죄책")
            else -> listOf("기쁨", "감사", "분노", "놀람", "슬픔")
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("오늘의 일기 미리보기", fontWeight = FontWeight.Bold)
        Text("“${dairy.take(40)}${if (dairy.length > 40) "..." else ""}”")

        Text("오늘의 감정은?  ${detected ?: "분석중"}", fontWeight = FontWeight.Medium)

        Text("감정 분류 선택", fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(emotionCats.size) { i ->
                val label = emotionCats[i]
                FilterChip(
                    selected = (selectedEmotion ?: detected) == label,
                    onClick = { viewModel.chooseEmotion(label) },
                    label = { Text(label) }
                )
            }
        }

        Text("상세 감정 스티커 선택", fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f)
        ) {
            items(detailStickers) { sticker ->
                AssistChip(
                    onClick = { viewModel.chooseSticker(sticker) },
                    label = { Text(sticker) },
                    modifier = Modifier
                        .padding(6.dp)
                        .border(
                            width = if (selectedSticker == sticker) 2.dp else 1.dp,
                            color = if (selectedSticker == sticker)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }

        Button(
            onClick = { bottomNavController.navigate(Routes.LetterToTomorrow) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("다음으로") }
    }
}
