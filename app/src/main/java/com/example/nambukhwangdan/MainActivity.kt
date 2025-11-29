package com.example.nambukhwangdan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.nambukhwangdan.navigation.AppNavHost
import com.example.nambukhwangdan.ui.theme.NambukhwangdanTheme
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val diaryViewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NambukhwangdanTheme {
                AppNavHost()
            }
        }
    }
}
