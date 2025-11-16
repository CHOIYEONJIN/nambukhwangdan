package com.example.nambukhwangdan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.nambukhwangdan.navigation.AppNavHost
import com.example.nambukhwangdan.ui.theme.NambukhwangdanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NambukhwangdanTheme {
                AppNavHost()
            }
            }
        }
    }
