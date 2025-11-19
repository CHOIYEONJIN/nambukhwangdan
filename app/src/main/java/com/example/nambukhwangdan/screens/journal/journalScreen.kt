package com.example.nambukhwangdan.screens.journal

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun JournalScreen(onWriteDiary: () -> Unit) {
    Button(onClick = onWriteDiary) {
        Text("일기 쓰기")
    }
}