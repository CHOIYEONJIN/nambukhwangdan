package com.example.lab04.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lab04.screens.TodoViewModel
import com.example.todoapplication.screens.AddEditTodoScreen
import com.example.todoapplication.screens.TodoListScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val viewModel: TodoViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            TodoListScreen(viewModel, navController)
        }
        composable("addEdit?id={id}") { backStackEntry ->
            AddEditTodoScreen(viewModel, navController, backStackEntry)
        }
        composable("addEdit") { backStackEntry ->
            AddEditTodoScreen(viewModel, navController, backStackEntry)
        }
    }
}
