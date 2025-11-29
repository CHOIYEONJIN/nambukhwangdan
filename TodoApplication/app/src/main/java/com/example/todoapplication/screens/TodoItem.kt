package com.example.todoapplication.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lab04.data.TodoEntity
import com.example.lab04.screens.TodoViewModel

@Composable
fun TodoItem(todo: TodoEntity, viewModel: TodoViewModel, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("addEdit?id=${todo.id}")
            }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = todo.isDone, onCheckedChange = { isChecked ->
                val updatedTodo = todo.copy(isDone = isChecked)
                viewModel.updateTodo(updatedTodo)
            })
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(text = todo.title, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
                Text(todo.memo, modifier = Modifier.padding(start = 8.dp), fontSize = 10.sp)
            }
            Text(todo.tag, modifier = Modifier.padding(start = 8.dp), fontSize = 10.sp)
            IconButton(onClick = { navController.navigate("addEdit?id=${todo.id}") }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Todo"
                )
            }
            IconButton(onClick = { viewModel.deleteTodo(todo) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Todo"
                )
            }
        }
    }
}