package com.example.todocrud.viewmodel

import android.R.attr.tag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.todocrud.model.Todo

class TodoViewModel : ViewModel() {
    //id 관리를 위한 int 변수 생성
    // todoList 및 id 변수느 외부 접근 불가
    // -> 오로지 TodoViewModel에서 정의한 함수를 통해서만 수정 가능 & viewmodel을 통해서 데이터 접근 가능
    private var nextId = 0
    var todoList = mutableStateListOf<Todo>()
        // 외부 접근을 막기 위해 private로 선언
        private set

    fun toggleTodo(id: Int) {
        val index = todoList.indexOfFirst { it.id == id }
        if (index != -1) {
            val todo = todoList[index]
            todoList[index] = todo.copy(isDone = !todo.isDone)
        }
    }
    // TodoList는 TodoViewModel 클래스에서만 접근 가능하므로 Todolist의 값 변경을 위한 함수도 TodoViewModel 클래스에 정의 해줘야함
    fun addTodo(title: String,tag:String?,memo : String?) {
        todoList.add(Todo(id = nextId++, title = title, tag=tag, memo=memo))
    }
    fun updateTodo(id: Int, title: String,tag:String?, memo : String?) {
        val index = todoList.indexOfFirst { it.id == id }
        if (index != -1) {
            //상태 감지를 통해 데이터 변경 시 ui업데이트를 )하기 위해 data class Todo로 title만 바뀐 복사본을 만들어 값을 넣어줌
            // todoList[index].title = "새 제목" <- 이렇게 적으면 todoList 내부 값을 변경하는거라 상태의 자동 업데이트가 안됨
            //왜냐면 선언한 변수(todoList)의 참조 변화가 감지될 때만 상태가 변했다고 판단하기 때문
            todoList[index] = todoList[index].copy(title = title,tag=tag,memo=memo)
        }
    }
    fun deleteTodo(id: Int) {
        todoList.removeAll { it.id == id }
    }
}