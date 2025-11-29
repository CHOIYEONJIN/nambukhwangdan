package com.example.todocrud.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.todocrud.viewmodel.TodoViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoScreen(
    viewModel: TodoViewModel,
    navController: NavController,
    backStackEntry: NavBackStackEntry
) {
    Scaffold(
    // FAB 버튼 설정
    topBar = {
        TopAppBar(
            title= {Text("Todo List")},
            actions={
                IconButton(onClick={}){
                    Icon(imageVector = Icons.Default.MoreVert,
                        contentDescription = "더보기 메뉴")
                }
            })
    }){innerPadding ->
    val todoId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
    val existingTodo = viewModel.todoList.find { it.id == todoId }
    var text by remember { mutableStateOf(existingTodo?.title ?: "") }
    var tagtext by remember { mutableStateOf(existingTodo?.tag ?: "") }
    var memotext by remember { mutableStateOf(existingTodo?.memo ?: "") }

    Column(modifier = Modifier.padding(innerPadding)) {
        //기존에 있었던 text가 있었다면 그 text값 띄우고 없었으면 암것도 안적혀있음
        TextField(value = text,
            onValueChange = { text = it }, label = { Text("할 일") },
            modifier = Modifier.fillMaxWidth().padding(8.dp))
        // 값 바뀌면 해당 text로 값 update
        Spacer(modifier = Modifier.height(4.dp))
        TextField(value = tagtext,
            onValueChange = { tagtext = it }, label = { Text("태그") },
            modifier = Modifier.fillMaxWidth().padding(8.dp))
        // 값 바뀌면 해당 text로 값 update
        Spacer(modifier = Modifier.height(4.dp))
        TextField(value = memotext,
            onValueChange = { memotext = it }, label = { Text("메모") },
            modifier = Modifier.fillMaxWidth().padding(8.dp))
        // 값 바뀌면 해당 text로 값 update
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = {
            // 저장 버튼 클릭 시
            if (todoId != null) {
                // 이미 있었던 Todo 수정하는 경우 새로 바뀐 text로 todo값 업데이트
                viewModel.updateTodo(todoId, text,tagtext,memotext)
            }
            else {
                // 새로운 todo 추가하는 경우 입력한 text로 새로운 todo 생성
                viewModel.addTodo(text,tagtext,memotext)
            }
            // 이전화면으로 돌아가기
            navController.popBackStack()
        }, modifier = Modifier.padding(8.dp)) {
            // button  속 text
            if (todoId!=null){
            Text("수정")
            }
            else{
                Text("저장")
            }
        }
//        if (todoId != null) {
//            // 이미 존재하는 todo일 경우에만
//            Spacer(modifier = Modifier.height(8.dp))
//            Button(onClick = {
//                // 삭제 버튼은 기존에 있는 todo였을 경우에만 보임
//                //삭제버튼 클릭 시 todoList에서 todo를 삭제함
//                viewModel.deleteTodo(todoId)
//                // 이전 화면으로 돌아가기
//                navController.popBackStack()
//            }) {
//                Text("삭제")
//            }
//        }
    }
}
}