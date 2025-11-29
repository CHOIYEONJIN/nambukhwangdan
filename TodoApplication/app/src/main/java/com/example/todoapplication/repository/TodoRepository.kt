package com.example.todoapplication.repository

import com.example.lab04.data.TodoDao
import com.example.lab04.data.TodoEntity
import javax.inject.Inject

class TodoRepository @Inject constructor(private val dao: TodoDao) {
    val allTodos = dao.getAll()

    suspend fun insert(todo: TodoEntity) = dao.insert(todo)
    suspend fun update(todo: TodoEntity) = dao.update(todo)
    suspend fun delete(todo: TodoEntity) = dao.delete(todo)
}
