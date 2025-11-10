package com.example.nambukhwangdan.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.nambukhwangdan.model.Diary


class DiaryViewModel : ViewModel() {
    private var nextId = 0
    var DiaryList = mutableStateListOf<Diary>()
        // 외부 접근을 막기 위해 private로 선언
        private set

    fun likeDiary(id: Int) {
        val index = DiaryList.indexOfFirst { it.id == id }
        if (index != -1) {
            val todo = DiaryList[index]
            DiaryList[index] = todo.copy(liked = !todo.liked)
        }
    }
    // TodoList는 TodoViewModel 클래스에서만 접근 가능하므로 Todolist의 값 변경을 위한 함수도 TodoViewModel 클래스에 정의 해줘야함
    fun addDiary(content: String,emotion:String, date:Long, sendToFuture : Boolean, sendTime:Long?) {
        DiaryList.add(Diary(id = nextId++, content = content, date=date, emotion = emotion, sendToFuture = sendToFuture ,sendTime=sendTime))
    }
}