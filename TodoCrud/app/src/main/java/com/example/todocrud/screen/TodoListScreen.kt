package com.example.todocrud.screen

import android.R.attr.contentDescription
import android.R.attr.onClick
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.todocrud.viewmodel.TodoViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(viewModel: TodoViewModel, navController: NavController) {
    // 화면 프레임 Scaffold (topbar-content-FAB-bottombar 구조)
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
                 },
        floatingActionButton = {
            // FAB버튼 클릭 시 addEdit 화면으로 넘어감
            FloatingActionButton(onClick = { navController.navigate("addEdit") }) {
                // FAB 버튼 속 내용
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Todo"
                )

            }
        }
    ) { padding ->
        // todo list를 lazycolumn으로 구성함
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(padding)) {
            items(viewModel.todoList) { todo ->
                Surface(
                    color=Color(0xFFE9E4FF),
                    shadowElevation = 4.dp,
                    shape= RoundedCornerShape(8.dp),
                    border= BorderStroke(2.dp,color=Color.Gray)
                ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 각 todo를 클릭 가능하게 해서 다른 화면으로 이동해 수정 및 삭제 가능하게 함
//                        .clickable {
//                            navController.navigate("addEdit?id=${todo.id}")
//                            //todo 수정 및 삭제 화면으로 이동함
//                        }
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = todo.isDone, onCheckedChange = { viewModel.toggleTodo(todo.id) })
                            Column(){
                                Text(text = todo.title,
                                    fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(5.dp))
                                if(todo.memo!=null){
                                    Text(text=todo.memo,
                                        fontSize = 16.sp)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically){
                            if (todo.tag!=null){
                                Text(text=todo.tag,
                                    fontSize = 13.sp,
                                    color=Color.DarkGray,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            IconButton(onClick = { navController.navigate("addEdit?id=${todo.id}") }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { viewModel.deleteTodo(todo.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
                }
            }
        }
    }
}