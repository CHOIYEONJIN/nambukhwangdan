package com.example.todocrud.model

data class Todo(
    val id: Int, // 고유 ID
    val title: String, // 제목
    val tag : String?, // 태그
    val memo : String?, // 메모
    val isDone: Boolean = false // 완료 여부
)