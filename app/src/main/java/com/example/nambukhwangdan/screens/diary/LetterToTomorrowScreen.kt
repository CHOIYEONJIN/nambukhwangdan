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
import androidx.compose.runtime.collectAsState
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
    val diary by viewModel.todayDiary.collectAsState()
    val dateMillis by viewModel.selectedDateMillis.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("내일의 나에게 보낼 편지", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = diary,
            onValueChange = { viewModel.updateDiary(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        Button(
            onClick = {
                viewModel.persistDiary(
                    content = diary,
                    sendToFuture = true,
                    dateMillis = dateMillis
                )
                navController.navigate(Routes.Home) {
                    popUpTo(Routes.MainHost)
                }
                navController.popBackStack(route = "diaryWrite", inclusive = false)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("저장하기") }
    }
}