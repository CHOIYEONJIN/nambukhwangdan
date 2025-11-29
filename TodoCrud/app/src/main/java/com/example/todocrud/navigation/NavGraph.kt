package com.example.todocrud.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.todocrud.screen.AddEditTodoScreen
import com.example.todocrud.screen.TodoListScreen
import com.example.todocrud.viewmodel.TodoViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    //navController가 이동할 수 있게 각 route별 이동 위치를 지정함
    // 상태 private 리스트인 todolist에 접근할 수 있는 viewmodel을 함께 제공함
    // -> viewmodel없이는 todolist의 데이터를 알 수 없음
    val viewModel: TodoViewModel = viewModel()
    NavHost(navController = navController, startDestination = "list") {
        // route 가 list일 떄 TodoListScreen으로 이동함
        // 할 일 목록 화면으로 이동한다
        composable("list") {
            TodoListScreen(viewModel, navController)
        }
        // route 가 addEdit?id={id}일 때 AddEditTodoScreen으로 이동함
        // Todo를 수정, 삭제 할 때 사용 -> addEdit?id={id}에서 id를 사용해 todo를 식별
        composable("addEdit?id={id}") { backStackEntry ->
            AddEditTodoScreen(viewModel, navController, backStackEntry)
        }
        // route 가 addEdit일 때 AddEditTodoScreen으로 이동함
        // 새로운 todo를 만드는 화면으로 이동할 때 사용하는 route
        composable("addEdit") { backStackEntry ->
            AddEditTodoScreen(viewModel, navController, backStackEntry)
        }
    }
}