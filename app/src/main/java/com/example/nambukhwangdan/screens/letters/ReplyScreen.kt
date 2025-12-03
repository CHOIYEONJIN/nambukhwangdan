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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nambukhwangdan.ui.theme.Background
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.viewmodel.LetterViewModel

@Composable
fun ReplyScreen(
    viewModel: LetterViewModel,
    navController: NavController
) {
    // 📌 Firestore에서 편지 최신화 (화면 열릴 때 한 번만 실행)
    LaunchedEffect(Unit) {
        viewModel.syncLettersFromFirestore()
    }

    // 🔹 pastLetters → allLetters로 변경
    val pastLetters = viewModel.allLetters.collectAsState().value
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
                val targetLetter = pastLetters.find { it.id == targetId }

                targetLetter?.let { letter ->
                    Text("${formatDate(letter.createdAt)}의 ${letter.nickname}에게서 온 편지")

                    ExpandableDiaryCard(letter.content)   // UI 유지 🍀
                }
            }

            Spacer(Modifier.height(12.dp))

            // 🔹 작성 영역
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
                viewModel.persistLetter()  // 저장
                navController.popBackStack()  // 이전 화면으로
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("답장 보내기", color = Color.White)
        }
    }
}
