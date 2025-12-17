package com.example.nambukhwangdan.screens.letters

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nambukhwangdan.screens.diary.ExpandableDiaryCard
import com.example.nambukhwangdan.screens.diary.formatDate
import com.example.nambukhwangdan.ui.theme.Primary
import com.example.nambukhwangdan.ui.theme.Surface
import com.example.nambukhwangdan.ui.theme.Variables
import com.example.nambukhwangdan.viewmodel.LetterViewModel
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyScreen(
    viewModel: LetterViewModel,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        viewModel.syncLettersFromFirestore()
    }
    val scope = rememberCoroutineScope()
    val allLetters by viewModel.allLetters.collectAsState()
    val content by viewModel.letterContent.collectAsState()
    val replyToId by viewModel.replyToId.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Variables.Color4) // 1번 예시 스크린의 배경색 적용
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단바 대용 (예시 스크린의 간격 유지)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "답장 하기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🔹 1. 답장 대상 편지 카드 (예시 스크린 스타일)
                replyToId?.let { targetId ->
                    val targetLetter = allLetters.find { it.id == targetId }
                    targetLetter?.let { letter ->
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 30.dp)
                                    .shadow(4.dp, RoundedCornerShape(10.dp))
                                    .fillMaxWidth()
                                    .background(Surface, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 20.dp, vertical = 15.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "${formatDate(letter.createdAt)}에 온 편지",
                                    fontSize = 13.sp,
                                    color = Primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                // 기존 카드 UI 활용
                                ExpandableDiaryCard(letter.content)
                            }
                        }
                    }
                }

                // 🔹 2. 답장 작성 박스 (예시 스크린의 TextField 스타일)
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 30.dp)
                            .shadow(4.dp, RoundedCornerShape(10.dp))
                            .fillMaxWidth()
                            .background(Surface, RoundedCornerShape(10.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "답장 작성",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                        )

                        TextField(
                            value = content,
                            onValueChange = { viewModel.updateContent(it) },
                            placeholder = {
                                Text(
                                    text = "답장을 입력해주세요",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif
                                )
                            },
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                fontFamily = FontFamily.SansSerif
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp) // 입력창 최소 높이 확보
                                .animateContentSize(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Surface,
                                unfocusedContainerColor = Surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Primary,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedPlaceholderColor = Color.Gray,
                                unfocusedPlaceholderColor = Color.LightGray
                            )
                        )
                    }
                }
            }
        }

        // 🔹 3. 하단 전송 버튼 (예시 스크린 스타일)
        Button(
            onClick = {
                scope.launch {
                    viewModel.sendReply()
                    viewModel.handleReplySentSuccessfully()
                }
                navController.popBackStack()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text("답장 보내기", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}