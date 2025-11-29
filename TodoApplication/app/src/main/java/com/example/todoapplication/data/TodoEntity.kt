package com.example.lab04.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val tag: String = "", // 태그
    val memo: String = "", // 메모
    val description: String = "", // 설명
    val isDone: Boolean = false // 완료 여부
)
