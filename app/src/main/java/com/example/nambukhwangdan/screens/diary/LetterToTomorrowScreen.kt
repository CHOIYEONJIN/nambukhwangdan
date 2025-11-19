package com.example.nambukhwangdan.screens.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nambukhwangdan.navigation.Routes
import com.example.nambukhwangdan.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterToTomorrowScreen(
    viewModel: DiaryViewModel,
    navController: NavController
) {
    var letter by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("내일의 나에게 보낼 편지", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = letter,
            onValueChange = { letter = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        Button(
            onClick = {
                // 저장 로직은 이후 Firestore 연동 시 구현
                // 여기선 흐름만 확인
                navController.navigate(Routes.Home) {
                    popUpTo(Routes.MainHost)
                }
                viewModel.clearForNewEntry()
                navController.popBackStack(route = "diaryWrite", inclusive = false)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("저장하기") }
    }
}