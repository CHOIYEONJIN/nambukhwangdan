package com.example.todoapplication.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(){
    TopAppBar(
        title = { Text("Todo List") },
        actions = {
            IconButton(onClick = { /* Handle settings click */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Settings")
            }
        }
    )
}