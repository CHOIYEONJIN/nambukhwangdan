package com.example.nambukhwangdan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
// ⭐️ 권한 요청을 위한 Import 추가
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nambukhwangdan.navigation.AppNavHost
import com.example.nambukhwangdan.ui.theme.NambukhwangdanTheme
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val diaryViewModel: DiaryViewModel by viewModels()

    // ⭐️ 권한 요청 상수 정의
    companion object {
        const val REQUEST_CODE_NOTIFICATION_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            NambukhwangdanTheme {
                AppNavHost()
            }
        }
    }
}