package com.folta.todoapp.data.local


interface ToDoRepository {
    suspend fun findByDate(dateyyMMDD: String): List<ToDo>
    suspend fun save(todo: ToDo)
}