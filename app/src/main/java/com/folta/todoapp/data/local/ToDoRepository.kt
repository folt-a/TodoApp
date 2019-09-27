package com.folta.todoapp.data.local


interface ToDoRepository {
    suspend fun findByDate(dateyyMMDD: String): List<ToDo>
    suspend fun find(Id: Int): ToDo?
    suspend fun save(todo: ToDo) : Int
    suspend fun saveSort(todos: Iterable<ToDo>)
    suspend fun delete(id: Int)
}