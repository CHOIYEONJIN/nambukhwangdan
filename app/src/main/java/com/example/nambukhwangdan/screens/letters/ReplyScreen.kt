package com.example.nambukhwangdan.screens.letters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nambukhwangdan.screens.diary.ExpandableDiaryCard
import com.example.nambukhwangdan.screens.diary.formatDate
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.viewmodel.LetterViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat // ⭐️ SimpleDateFormat import 추가
import java.util.Date
import java.util.Locale


@Composable
fun ReplyScreen(
    viewModel: LetterViewModel,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        viewModel.syncLettersFromFirestore()
    }
    val scope = rememberCoroutineScope()
    val allLetters = viewModel.allLetters.collectAsState().value
    val content by viewModel.letterContent.collectAsState()
    val replyToId by viewModel.replyToId.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(bottom = 120.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔹 답장 대상 편지 표시
            replyToId?.let { targetId ->
                // allLetters에서 답장 대상 ID와 일치하는 편지를 찾습니다.
                val targetLetter = allLetters.find { it.id == targetId }

                targetLetter?.let { letter ->
                    Text(
                        // 닉네임은 발신자의 닉네임으로 가정합니다.
                        "${formatDate(letter.createdAt)}에 ${letter.nickname}님에게서 온 편지",
                        modifier = Modifier.padding(top = 20.dp)
                    )

                    ExpandableDiaryCard(letter.content)   // 원본 편지 내용을 보여줍니다.
                }
            }

            Spacer(Modifier.height(12.dp))

            // 🔹 답장 작성 영역
            TextField(
                value = content,
                onValueChange = { viewModel.updateContent(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .wrapContentHeight(),
                placeholder = { Text("답장을 작성해주세요") }
            )
        }

        // 🔹 전송 버튼
        Button(
            onClick = {
                scope.launch {
                    viewModel.persistLetter()
                    viewModel.handleReplySentSuccessfully()
                }
                navController.popBackStack()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("답장 보내기", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}