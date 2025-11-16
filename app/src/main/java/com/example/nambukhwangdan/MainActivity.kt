package com.example.nambukhwangdan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.nambukhwangdan.navigation.NavGraph
import com.example.nambukhwangdan.ui.theme.NambukhwangdanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NambukhwangdanTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
            }
        }
    }
