package com.example.nambukhwangdan.screens.diary

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nambukhwangdan.viewmodel.DiaryViewModel
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import coil.compose.AsyncImage
@Composable
fun PhotoSelectScreen(
    viewModel: DiaryViewModel,
    navController: NavController
) {
    val all = viewModel.allPhotos
    val selected by viewModel.selectedPhotos.collectAsState()

    Column(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            items(all) { url ->
                val isSel = selected.contains(url)
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .border(
                            width = if (isSel) 3.dp else 1.dp,
                            color = if (isSel) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                        .clickable { viewModel.togglePhoto(url) }
                        .fillMaxWidth()
                        .height(110.dp)
                )
            }
        }
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) { Text("선택완료") }
    }
}

@Composable
fun AsyncImage(model: Int, contentDescription: Nothing?, modifier: Modifier) {
    TODO("Not yet implemented")
}